package com.minilab.schedule;

import com.minilab.schedule.AutoScheduler.Assignment;
import com.minilab.schedule.AutoScheduler.Booking;
import com.minilab.schedule.AutoScheduler.Demand;
import com.minilab.schedule.AutoScheduler.Plan;
import com.minilab.schedule.AutoScheduler.Resource;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 不需要 Spring context 也不需要資料庫：排程規則是純函式，測試就該跑得起來。
 */
class AutoSchedulerTest {

    private static final LocalDateTime T9 = at(9);
    private static final LocalDateTime T10 = at(10);
    private static final LocalDateTime T11 = at(11);
    private static final LocalDateTime T12 = at(12);
    private static final Duration ONE_HOUR = Duration.ofHours(1);

    private final AutoScheduler scheduler = new AutoScheduler();

    private static LocalDateTime at(int hour) {
        return LocalDateTime.of(2026, 9, 1, hour, 0);
    }

    private static Resource res(int id, boolean usable, String... tags) {
        return new Resource(id, Set.of(tags), usable);
    }

    /** 一小時、一台機、九點以後、沒有期限——多數測試只在乎「挑了誰、什麼時候」。 */
    private static Demand demand(String id, String... tags) {
        return new Demand(id, Set.of(tags), ONE_HOUR, T9, null, 1);
    }

    private Plan plan(List<Demand> demands, List<Resource> emps, List<Resource> machines) {
        return scheduler.schedule(demands, emps, machines, List.of(), List.of());
    }

    @Test
    void 行事曆空著時就從最早可開始的時間排下去() {
        Plan p = plan(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")));

        assertEquals(List.of(new Assignment("r1", 1, List.of(100), T9, T10)), p.assignments());
        assertTrue(p.rejections().isEmpty());
    }

    @Test
    void 唯一人選被佔用時往後順延到他有空為止() {
        Plan p = scheduler.schedule(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")),
                List.of(new Booking(1, T9, T11)), List.of());

        assertEquals(T11, p.assignments().get(0).start());
        assertEquals(T12, p.assignments().get(0).end());
    }

    @Test
    void 有別人有空時優先挑更早的時段而不是等同一個人() {
        Plan p = scheduler.schedule(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性"), res(2, true, "電性")), List.of(res(100, true, "電性")),
                List.of(new Booking(1, T9, T11)), List.of());

        assertEquals(2, p.assignments().get(0).empId());
        assertEquals(T9, p.assignments().get(0).start());
    }

    @Test
    void 機台被佔用一樣會順延() {
        Plan p = scheduler.schedule(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")),
                List.of(), List.of(new Booking(100, T9, T10)));

        assertEquals(T10, p.assignments().get(0).start());
    }

    @Test
    void 資源只有一份時同批任務會自動排成前後接續() {
        Plan p = plan(List.of(demand("r1", "電性"), demand("r2", "電性")),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")));

        assertEquals(2, p.assignments().size());
        assertTrue(p.rejections().isEmpty());
        assertEquals(T9, p.assignments().get(0).start());
        assertEquals(T10, p.assignments().get(1).start());
    }

    @Test
    void 時段相接不算衝突() {
        Plan p = scheduler.schedule(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")),
                List.of(new Booking(1, at(8), T9)), List.of());

        assertEquals(T9, p.assignments().get(0).start());
    }

    @Test
    void 期限之前排不下就退件() {
        Demand tight = new Demand("r1", Set.of("電性"), ONE_HOUR, T9, T10, 1);
        Plan p = scheduler.schedule(List.of(tight),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")),
                List.of(new Booking(1, T9, T11)), List.of());

        assertTrue(p.assignments().isEmpty());
        assertTrue(p.rejections().get(0).reason().contains("期限"));
    }

    @Test
    void 期限剛好放得下就排進去() {
        Demand exact = new Demand("r1", Set.of("電性"), ONE_HOUR, T9, T10, 1);
        Plan p = plan(List.of(exact), List.of(res(1, true, "電性")), List.of(res(100, true, "電性")));

        assertEquals(T9, p.assignments().get(0).start());
        assertEquals(T10, p.assignments().get(0).end());
    }

    @Test
    void 技能不符的人不會被派工() {
        Plan p = plan(List.of(demand("r1", "電性")),
                List.of(res(1, true, "物性")), List.of(res(100, true, "電性")));

        assertTrue(p.assignments().isEmpty());
        assertTrue(p.rejections().get(0).reason().contains("人員"));
    }

    @Test
    void 任務要求多個技能時要全部符合() {
        Plan p = plan(List.of(demand("r1", "電性", "高溫")),
                List.of(res(1, true, "電性"), res(2, true, "電性", "高溫", "物性")),
                List.of(res(100, true, "電性", "高溫")));

        assertEquals(2, p.assignments().get(0).empId());
    }

    @Test
    void 標記為不可用的人力物力會被跳過() {
        Plan p = plan(List.of(demand("r1", "電性")),
                List.of(res(1, false, "電性"), res(2, true, "電性")),
                List.of(res(100, false, "電性"), res(101, true, "電性")));

        assertEquals(2, p.assignments().get(0).empId());
        assertEquals(List.of(101), p.assignments().get(0).machineIds());
    }

    @Test
    void 一個人同時段可以借走多台機器() {
        Demand threeMachines = new Demand("r1", Set.of("電性"), ONE_HOUR, T9, null, 3);
        Plan p = plan(List.of(threeMachines), List.of(res(1, true, "電性")),
                List.of(res(100, true, "電性"), res(101, true, "電性"), res(102, true, "電性")));

        assertEquals(3, p.assignments().get(0).machineIds().size());
        assertEquals(1, p.assignments().get(0).empId());
    }

    @Test
    void 合格機台總數不足時退件並說明原因() {
        Demand threeMachines = new Demand("r1", Set.of("電性"), ONE_HOUR, T9, null, 3);
        Plan p = plan(List.of(threeMachines),
                List.of(res(1, true, "電性")), List.of(res(100, true, "電性")));

        assertTrue(p.assignments().isEmpty());
        assertTrue(p.rejections().get(0).reason().contains("需要 3 台"));
    }

    @Test
    void 同一時段不會把一台機器給兩個人() {
        Plan p = plan(List.of(demand("r1", "電性"), demand("r2", "電性")),
                List.of(res(1, true, "電性"), res(2, true, "電性")),
                List.of(res(100, true, "電性")));

        Assignment a = p.assignments().get(0);
        Assignment b = p.assignments().get(1);
        assertEquals(List.of(100), a.machineIds());
        assertEquals(List.of(100), b.machineIds());
        assertTrue(!a.start().isBefore(b.end()) || !b.start().isBefore(a.end()),
                "同一台機器被排進兩個重疊的時段");
    }

    @Test
    void 同樣有空時挑目前工時最少的人() {
        Plan p = scheduler.schedule(List.of(demand("r1", "電性")),
                List.of(res(1, true, "電性"), res(2, true, "電性")), List.of(res(100, true, "電性")),
                List.of(new Booking(1, at(7), at(8))),  // 1 號昨天已經扛了一小時
                List.of());

        assertEquals(2, p.assignments().get(0).empId());
        assertEquals(T9, p.assignments().get(0).start());
    }

    @Test
    void 任務長度不是正數就退件() {
        Demand zero = new Demand("r1", Set.of("電性"), Duration.ZERO, T9, null, 1);
        Plan p = plan(List.of(zero), List.of(res(1, true, "電性")), List.of(res(100, true, "電性")));

        assertTrue(p.rejections().get(0).reason().contains("任務長度"));
    }

    @Test
    void 同樣的輸入永遠得到同樣的結果() {
        List<Demand> demands = List.of(demand("r1", "電性"), demand("r2", "電性"));
        List<Resource> emps = List.of(res(1, true, "電性"), res(2, true, "電性"));
        List<Resource> machines = List.of(res(100, true, "電性"), res(101, true, "電性"));

        assertEquals(plan(demands, emps, machines), plan(demands, emps, machines));
    }

    @Test
    void 可用人力吃緊時先滿足受限最緊的需求() {
        // r2 只有 2 號做得來；r1 兩個人都行。先把 2 號給 r1 就會逼 r2 往後延。
        Plan p = plan(List.of(demand("r1", "電性"), demand("r2", "高溫")),
                List.of(res(1, true, "電性"), res(2, true, "電性", "高溫")),
                List.of(res(100, true, "電性", "高溫"), res(101, true, "電性", "高溫")));

        assertEquals(T9, p.assignments().get(0).start());
        assertEquals(T9, p.assignments().get(1).start());
        assertEquals(2, p.assignments().get(1).empId());
        assertNotEquals(p.assignments().get(0).empId(), p.assignments().get(1).empId());
    }
}
