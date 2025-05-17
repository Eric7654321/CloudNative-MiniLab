package com.minilab.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minilab.mapper.EmpMapper;
import com.minilab.mapper.MachineMapper;
import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    TaskMapper taskMapper;
    @Autowired
    EmpMapper empMapper;
    @Autowired
    MachineMapper machineMapper;

    @Override
    public List<Task> getTaskByGroupId(String groupId) {
        List<Task> tasks = taskMapper.getTaskByGroupId(groupId);
        log.info("已查詢到tasks: {}, groupId={}", tasks, groupId);
        return tasks;
    }

    @Override
    public List<Task> getTaskByIdAndTime(Integer id, LocalDate now) {
        List<Task> tasks = taskMapper.getTaskByIdAndTime(id, now, now);
        log.info("已查詢到tasks: {}, id= {}, day= {}", tasks, id);
        return List.of();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result tasksValidateAndCheck(List<Task> tasks) {
        Result result = tasksCheck(tasks);
        if(result.getCode() != 1){
            return Result.error(result.getMsg());
        }

        // 收集所有 machineId
        Set<Integer> allMachineIds = new HashSet<>();
        for (Task task : tasks) {
            String[] ids = task.getMachine().replaceAll("[\\[\\]\\s]", "").split(",");
            for (String idStr : ids) {
                try {
                    allMachineIds.add(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
        }

        // 批量查出所有機器名稱
        List<Machine> machines = machineMapper.getMachinesByIds(new ArrayList<>(allMachineIds));
        Map<Integer, String> idToName = machines.stream()
                .collect(Collectors.toMap(Machine::getId, Machine::getMachineName));

        // 為每個任務填入 empName 與 machineName
        for (Task task : tasks) {
            task.setUpdateTime(LocalDateTime.now());

            // empName
            task.setEmpName(empMapper.getEmpById(task.getEmp()).getUsername());

            // machineName
            String[] ids = task.getMachine().replaceAll("[\\[\\]\\s]", "").split(",");
            List<String> names = new ArrayList<>();
            for (String idStr : ids) {
                try {
                    int id = Integer.parseInt(idStr);
                    if (idToName.containsKey(id)) {
                        names.add(idToName.get(id));
                    }
                } catch (NumberFormatException ignored) {}
            }
            task.setMachineName(String.join(",", names));

            // 寫入資料庫
            taskMapper.insertTask(task);
        }

        return Result.success();
    }


    @Override
    public List<Task> getTaskByIdAndTimeWeeks(Integer id, LocalDate now) {
        List<Task> tasks = taskMapper.getTaskByIdAndTime(id, now, now.plusDays(14));
        log.info("已查詢到兩個禮拜內tasks: {}, id= {}, day= {}", tasks, id);
        return List.of();
    }

    @Override
    public void deleteTask(Task task) {
        taskMapper.deleteTaskById(task.getId());
    }

    @Override
    public void updateTask(Task task) {
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateTask(task);
    }
    @Override
    public Result tasksCheck(List<Task> tasks) {
        //GPT寫的 記得test
        for (Task task : tasks) {
            // 1. 檢查工人是否具備該標籤
            String empTags = empMapper.setTagsByUserId(task.getEmp());
            if (empTags == null || !Arrays.asList(empTags.split(",")).contains(task.getTag())) {
                return Result.error("工人不具備任務所需的技能：" + task.getEmpName());
            }

            // 2. 檢查機器是否具備該標籤
            List<Integer> machineIds;
            try {
                machineIds = new ObjectMapper().readValue(task.getMachine(), new TypeReference<List<Integer>>() {});
            } catch (Exception e) {
                return Result.error("機器 ID 格式錯誤：" + task.getMachine());
            }

            List<Machine> machines = machineMapper.getMachinesByIds(machineIds);
            for (Machine machine : machines) {
                String tags = machineMapper.setTagsByUserId(machine.getId());
                if (tags == null || !Arrays.asList(tags.split(",")).contains(task.getTag())) {
                    return Result.error("機器【" + machine.getMachineName() + "】不支援此任務所需技能");
                }
            }

            // 3. 檢查工人在時間內是否已有其他任務
            List<Task> empTasks = taskMapper.getTaskByIdAndTime(
                    task.getEmp(),
                    task.getStartTime().toLocalDate(),
                    task.getEndTime().toLocalDate());

            for (Task t : empTasks) {
                if (isTimeOverlap(task, t)) {
                    return Result.error("工人 " + task.getEmpName() + " 時間重疊，與任務編號：" + t.getId());
                }
            }

            // 4. 檢查機器是否被其他任務佔用
            for (Integer machineId : machineIds) {
                List<Task> allTasks = taskMapper.getTaskByGroupId(task.getGroup());
                for (Task t : allTasks) {
                    List<Integer> tMachineIds = parseMachineIds(t.getMachine());
                    if (tMachineIds.contains(machineId) && isTimeOverlap(task, t)) {
                        return Result.error("機器 ID: " + machineId + " 被佔用，與任務編號：" + t.getId());
                    }
                }
            }
        }

        return Result.success();
    }

    private boolean isTimeOverlap(Task t1, Task t2) {
        return !(t1.getEndTime().isBefore(t2.getStartTime()) || t1.getStartTime().isAfter(t2.getEndTime()));
    }

    private List<Integer> parseMachineIds(String machineJson) {
        try {
            return new ObjectMapper().readValue(machineJson, new TypeReference<List<Integer>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

}
