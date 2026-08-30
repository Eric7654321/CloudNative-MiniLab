package com.minilab.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minilab.mapper.EmpMapper;
import com.minilab.mapper.MachineMapper;
import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.dto.AutoPlanRequest;
import com.minilab.pojo.dto.AutoPlanResponse;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.schedule.AutoScheduler;
import com.minilab.service.AutoScheduleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 把資料庫裡的人、機台與既有任務翻譯成 {@link AutoScheduler} 看得懂的形狀，算完再翻譯回 Task。
 * 排程規則本身一行都不在這裡——這一層只做 I/O 與轉換。
 */
@Slf4j
@Service
public class AutoScheduleServiceImpl implements AutoScheduleService {

    @Autowired
    EmpMapper empMapper;
    @Autowired
    MachineMapper machineMapper;
    @Autowired
    TaskMapper taskMapper;

    private final AutoScheduler scheduler = new AutoScheduler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result plan(String group, List<AutoPlanRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Result.error("沒有待排程的任務");
        }

        List<EmpVO> emps = empMapper.getEmpByGroupId(group);
        List<MachineVO> machines = machineMapper.getMachineByGroupId(group);
        if (emps.isEmpty() || machines.isEmpty()) {
            return Result.error("這個 group 沒有可用的人員或機台");
        }

        List<AutoScheduler.Resource> empPool = new ArrayList<>();
        Map<Integer, String> empNames = new HashMap<>();
        for (EmpVO emp : emps) {
            empPool.add(new AutoScheduler.Resource(emp.getId(),
                    parseTags(empMapper.setTagsByUserId(emp.getId())), isUsable(emp.getUsable())));
            empNames.put(emp.getId(), emp.getUsername());
        }

        List<AutoScheduler.Resource> machinePool = new ArrayList<>();
        Map<Integer, String> machineNames = new HashMap<>();
        for (MachineVO machine : machines) {
            machinePool.add(new AutoScheduler.Resource(machine.getId(),
                    parseTags(machineMapper.setTagsByUserId(machine.getId())), isUsable(machine.getUsable())));
            machineNames.put(machine.getId(), machine.getMachineName());
        }

        List<AutoScheduler.Booking> empBookings = new ArrayList<>();
        List<AutoScheduler.Booking> machineBookings = new ArrayList<>();
        for (Task task : taskMapper.getTaskByGroupId(group)) {
            if (task.getIsFinish() != null && task.getIsFinish() == 1) continue;
            if (task.getStartTime() == null || task.getEndTime() == null) continue;
            if (task.getEmp() != null) {
                empBookings.add(new AutoScheduler.Booking(task.getEmp(), task.getStartTime(), task.getEndTime()));
            }
            for (Integer machineId : parseMachineIds(task.getMachine())) {
                machineBookings.add(new AutoScheduler.Booking(machineId, task.getStartTime(), task.getEndTime()));
            }
        }

        Map<String, AutoPlanRequest> byId = new HashMap<>();
        List<AutoScheduler.Demand> demands = new ArrayList<>();
        for (AutoPlanRequest request : requests) {
            String id = request.getRequestId();
            if (id == null || id.isBlank() || byId.containsKey(id)) {
                return Result.error("每筆需求都需要唯一的 requestId，收到：" + id);
            }
            byId.put(id, request);
            if (request.getDurationMinutes() == null) {
                return Result.error("需求 " + id + " 沒有填任務長度");
            }
            int machineCount = request.getMachineCount() == null ? 1 : request.getMachineCount();
            // 沒指定最早開始時間就從現在起算——排程排到過去沒有意義。
            LocalDateTime earliest = request.getEarliestStart() == null
                    ? LocalDateTime.now() : request.getEarliestStart();
            demands.add(new AutoScheduler.Demand(id, splitTags(request.getTag()),
                    Duration.ofMinutes(request.getDurationMinutes()),
                    earliest, request.getDeadline(), machineCount));
        }

        AutoScheduler.Plan plan = scheduler.schedule(demands, empPool, machinePool, empBookings, machineBookings);
        log.info("自動排程完成，group={}，成功 {} 筆、退回 {} 筆",
                group, plan.assignments().size(), plan.rejections().size());

        List<Task> assigned = new ArrayList<>();
        for (AutoScheduler.Assignment a : plan.assignments()) {
            assigned.add(toTask(byId.get(a.requestId()), a, empNames, machineNames));
        }
        List<AutoPlanResponse.Rejected> rejected = plan.rejections().stream()
                .map(r -> new AutoPlanResponse.Rejected(r.requestId(), r.reason()))
                .collect(Collectors.toList());

        return Result.success(new AutoPlanResponse(assigned, rejected));
    }

    /** 回傳的 Task 尚未寫入資料庫：組長在畫面上確認後，仍然走既有的 /schedule/auto/ack。 */
    private Task toTask(AutoPlanRequest request, AutoScheduler.Assignment assignment,
                        Map<Integer, String> empNames, Map<Integer, String> machineNames) {
        Task task = new Task();
        task.setEmp(assignment.empId());
        task.setEmpName(empNames.get(assignment.empId()));
        // 既有資料把 machine 存成字串陣列（parseMachineIds 讀的是 List<String>），沿用同一個形狀，
        // 免得新舊兩種寫法在同一張表裡並存。
        task.setMachine(writeJson(assignment.machineIds().stream()
                .map(String::valueOf).collect(Collectors.toList())));
        task.setMachineName(writeJson(assignment.machineIds().stream()
                .map(machineNames::get).collect(Collectors.toList())));
        task.setStartTime(assignment.start());
        task.setEndTime(assignment.end());
        task.setTag(request.getTag());
        task.setDescription(request.getDescription());
        task.setGroup(request.getGroup());
        task.setIsFinish(0);
        task.setUpdateTime(LocalDateTime.now());
        return task;
    }

    private boolean isUsable(Integer usable) {
        return usable != null && usable == 1;
    }

    private Set<String> splitTags(String tag) {
        if (tag == null || tag.isBlank()) return Set.of();
        return Arrays.stream(tag.split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toSet());
    }

    /** tags 欄位是 JSON 陣列字串；沒設過標籤的人欄位會是 null，那不是錯誤，是「什麼都不會」。 */
    private Set<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.isBlank()) return Set.of();
        try {
            return new HashSet<>(objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            log.warn("標籤格式無法解析，視為沒有標籤：{}", tagsJson);
            return Set.of();
        }
    }

    private List<Integer> parseMachineIds(String input) {
        if (input == null || input.isBlank()) return List.of();
        try {
            if (input.startsWith("[") && input.endsWith("]")) {
                return objectMapper.readValue(input, new TypeReference<List<String>>() {})
                        .stream().map(Integer::parseInt).collect(Collectors.toList());
            }
            return List.of(Integer.parseInt(input.trim()));
        } catch (Exception e) {
            log.warn("機器欄位無法解析，該筆佔用不納入計算：{}", input);
            return List.of();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("機器欄位轉 JSON 失敗", e);
        }
    }
}
