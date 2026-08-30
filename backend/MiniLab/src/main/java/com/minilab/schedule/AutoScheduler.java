package com.minilab.schedule;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * 把「一批只寫了需求的任務」排進行事曆：挑人、挑機台，並且挑一個大家都有空的時段。
 *
 * 呼叫端只需要說「這件事要什麼技能、做多久、幾台機器、最早什麼時候能開始」，
 * 誰做、用哪幾台、什麼時候做都由這裡決定。
 *
 * 這個類別刻意不碰 Spring、MyBatis 與 Jackson：排程規則是這個系統唯一真正的商業邏輯，
 * 綁上框架就只能靠一個活的資料庫才驗得起來。
 *
 * 規則與 {@code TaskServiceImpl.tasksCheck} 同源：
 * 一個人同一時段可以借多台機器；一台機器同一時段只能被一個人借走；
 * 任務要求的技能必須是被指派者（人與機台皆然）技能的子集合。
 * 時間區間採半開區間 [start, end)，與 SQL 的 {@code NOT (end <= start OR start >= end)} 一致，
 * 所以前一件事 11:00 結束、下一件 11:00 開始不算衝突。
 */
public class AutoScheduler {

    /** 人員或機台。兩者在排程眼中的形狀相同，不必分成兩個型別。 */
    public record Resource(Integer id, Set<String> tags, boolean usable) {}

    /** 已經佔用掉的時段，來自資料庫裡尚未完成的任務。 */
    public record Booking(Integer resourceId, LocalDateTime start, LocalDateTime end) {}

    /**
     * 一筆待排的需求：只描述要什麼，不描述給誰、什麼時候。
     *
     * @param duration      這件事要做多久
     * @param earliestStart 最早不早於這個時間開始
     * @param deadline      最晚必須結束的時間，null 表示沒有期限
     * @param machineCount  需要幾台機器（人固定一位，因為 task 表一筆只掛一個 emp）
     */
    public record Demand(String requestId, Set<String> requiredTags, Duration duration,
                         LocalDateTime earliestStart, LocalDateTime deadline, int machineCount) {}

    /** 排程結果：誰、哪幾台、什麼時候。 */
    public record Assignment(String requestId, Integer empId, List<Integer> machineIds,
                             LocalDateTime start, LocalDateTime end) {}

    /** 排不進去的需求，附上人看得懂的原因。 */
    public record Rejection(String requestId, String reason) {}

    public record Plan(List<Assignment> assignments, List<Rejection> rejections) {}

    public Plan schedule(List<Demand> demands, List<Resource> emps, List<Resource> machines,
                         List<Booking> empBookings, List<Booking> machineBookings) {

        Map<Integer, List<Booking>> empBusy = groupByResource(empBookings);
        Map<Integer, List<Booking>> machineBusy = groupByResource(machineBookings);

        // 既有任務的時數一起算進負載，否則「最閒者承接」只會在這一批之內公平，
        // 對已經排滿的人不公平。
        Map<Integer, Long> empLoad = initialLoad(emps, empBookings);
        Map<Integer, Long> machineLoad = initialLoad(machines, machineBookings);

        // 排序：先做「有期限」與「合格人選最少」的。稀有人力被先來後到吃掉，
        // 後面那些只有一兩個人做得來的需求就再也排不進去了。
        List<Demand> ordered = new ArrayList<>(demands);
        ordered.sort(Comparator
                .comparingInt((Demand d) -> qualified(emps, d).size())
                .thenComparing(d -> d.deadline() == null ? LocalDateTime.MAX : d.deadline())
                .thenComparing(Demand::earliestStart)
                .thenComparing(Demand::requestId));

        List<Assignment> assignments = new ArrayList<>();
        List<Rejection> rejections = new ArrayList<>();

        for (Demand demand : ordered) {
            String invalid = validate(demand);
            if (invalid != null) {
                rejections.add(new Rejection(demand.requestId(), invalid));
                continue;
            }

            List<Resource> empPool = qualified(emps, demand);
            List<Resource> machinePool = qualified(machines, demand);
            if (empPool.isEmpty()) {
                rejections.add(new Rejection(demand.requestId(),
                        "沒有具備技能 " + demand.requiredTags() + " 且可用的人員"));
                continue;
            }
            if (machinePool.size() < demand.machineCount()) {
                rejections.add(new Rejection(demand.requestId(),
                        "具備技能 " + demand.requiredTags() + " 的可用機台只有 "
                                + machinePool.size() + " 台，需要 " + demand.machineCount() + " 台"));
                continue;
            }

            Assignment placed = placeEarliest(demand, empPool, machinePool,
                    empBusy, machineBusy, empLoad, machineLoad);
            if (placed == null) {
                rejections.add(new Rejection(demand.requestId(), demand.deadline() == null
                        ? "找不到人員與機台同時有空的時段"
                        : "期限 " + demand.deadline() + " 之前找不到人員與機台同時有空的時段"));
                continue;
            }

            long minutes = demand.duration().toMinutes();
            book(empBusy, empLoad, placed.empId(), placed.start(), placed.end(), minutes);
            for (Integer machineId : placed.machineIds()) {
                book(machineBusy, machineLoad, machineId, placed.start(), placed.end(), minutes);
            }
            assignments.add(placed);
        }

        assignments.sort(Comparator.comparing(Assignment::requestId));
        rejections.sort(Comparator.comparing(Rejection::requestId));
        return new Plan(assignments, rejections);
    }

    /**
     * 找最早可行的開始時間。
     *
     * 只需要試 earliestStart 以及每一段既有佔用的結束點：任何可行的擺放都可以往前推到
     * 撞上某個佔用的結尾為止，所以最佳解一定落在這些點上，不必逐分鐘掃描。
     */
    private Assignment placeEarliest(Demand demand, List<Resource> empPool, List<Resource> machinePool,
                                     Map<Integer, List<Booking>> empBusy, Map<Integer, List<Booking>> machineBusy,
                                     Map<Integer, Long> empLoad, Map<Integer, Long> machineLoad) {

        TreeSet<LocalDateTime> candidates = new TreeSet<>();
        candidates.add(demand.earliestStart());
        collectReleaseTimes(empPool, empBusy, demand.earliestStart(), candidates);
        collectReleaseTimes(machinePool, machineBusy, demand.earliestStart(), candidates);

        for (LocalDateTime start : candidates) {
            LocalDateTime end = start.plus(demand.duration());
            if (demand.deadline() != null && end.isAfter(demand.deadline())) break;

            List<Resource> freeEmps = freeDuring(empPool, empBusy, start, end);
            if (freeEmps.isEmpty()) continue;
            List<Resource> freeMachines = freeDuring(machinePool, machineBusy, start, end);
            if (freeMachines.size() < demand.machineCount()) continue;

            freeEmps.sort(loadThenId(empLoad));
            freeMachines.sort(loadThenId(machineLoad));
            List<Integer> picked = new ArrayList<>();
            for (int i = 0; i < demand.machineCount(); i++) {
                picked.add(freeMachines.get(i).id());
            }
            return new Assignment(demand.requestId(), freeEmps.get(0).id(), picked, start, end);
        }
        return null;
    }

    private void collectReleaseTimes(List<Resource> pool, Map<Integer, List<Booking>> busy,
                                     LocalDateTime earliest, TreeSet<LocalDateTime> out) {
        for (Resource r : pool) {
            for (Booking b : busy.getOrDefault(r.id(), List.of())) {
                if (!b.end().isBefore(earliest)) out.add(b.end());
            }
        }
    }

    private String validate(Demand demand) {
        if (demand.machineCount() < 1) return "任務至少要用到一台機器";
        if (demand.duration() == null || demand.duration().isZero() || demand.duration().isNegative()) {
            return "任務長度必須大於零";
        }
        if (demand.deadline() != null
                && demand.earliestStart().plus(demand.duration()).isAfter(demand.deadline())) {
            return "期限太早，光是任務長度就放不下";
        }
        return null;
    }

    /** 具備全部所需技能且標記為可用的資源；還沒看時間。 */
    private List<Resource> qualified(List<Resource> pool, Demand demand) {
        List<Resource> out = new ArrayList<>();
        for (Resource r : pool) {
            if (r.usable() && r.tags().containsAll(demand.requiredTags())) out.add(r);
        }
        return out;
    }

    private List<Resource> freeDuring(List<Resource> pool, Map<Integer, List<Booking>> busy,
                                      LocalDateTime start, LocalDateTime end) {
        List<Resource> out = new ArrayList<>();
        for (Resource r : pool) {
            if (!overlaps(busy.get(r.id()), start, end)) out.add(r);
        }
        return out;
    }

    private boolean overlaps(List<Booking> bookings, LocalDateTime start, LocalDateTime end) {
        if (bookings == null) return false;
        for (Booking b : bookings) {
            if (b.start().isBefore(end) && start.isBefore(b.end())) return true;
        }
        return false;
    }

    /** 負載相同時用 id 決勝，讓同一批輸入永遠得到同一份結果（測試與稽核都需要）。 */
    private Comparator<Resource> loadThenId(Map<Integer, Long> load) {
        return Comparator
                .comparingLong((Resource r) -> load.getOrDefault(r.id(), 0L))
                .thenComparing(Resource::id);
    }

    private void book(Map<Integer, List<Booking>> busy, Map<Integer, Long> load, Integer resourceId,
                      LocalDateTime start, LocalDateTime end, long minutes) {
        busy.computeIfAbsent(resourceId, k -> new ArrayList<>()).add(new Booking(resourceId, start, end));
        load.merge(resourceId, minutes, Long::sum);
    }

    private Map<Integer, List<Booking>> groupByResource(List<Booking> bookings) {
        Map<Integer, List<Booking>> out = new HashMap<>();
        for (Booking b : bookings) {
            out.computeIfAbsent(b.resourceId(), k -> new ArrayList<>()).add(b);
        }
        return out;
    }

    private Map<Integer, Long> initialLoad(List<Resource> pool, List<Booking> bookings) {
        Map<Integer, Long> load = new HashMap<>();
        Set<Integer> known = new HashSet<>();
        for (Resource r : pool) known.add(r.id());
        for (Booking b : bookings) {
            if (!known.contains(b.resourceId())) continue;
            load.merge(b.resourceId(), Duration.between(b.start(), b.end()).toMinutes(), Long::sum);
        }
        return load;
    }
}
