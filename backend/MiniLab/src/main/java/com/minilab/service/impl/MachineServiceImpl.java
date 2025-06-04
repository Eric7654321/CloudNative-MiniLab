package com.minilab.service.impl;

import com.minilab.mapper.MachineMapper;
import com.minilab.mapper.TagMapper;
import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.MachineTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MachineServiceImpl implements MachineService {

    @Autowired
    MachineMapper machineMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    TagMapper tagMapper;

    @Override
    public List<MachineVO> getMachineByGroupId(String groupId) {
        List<MachineVO> machines = machineMapper.getMachineByGroupId(groupId);
        log.info("已查詢到machines: {}, groupId={}, 接著查詢tags", machines, groupId);
        for(MachineVO machine : machines) {
            machine.setTags(machineMapper.setTagsByUserId(machine.getId()));
        }
        return machines;
    }

    @Override
    @Transactional
    public void insert(Machine machine) {
        machine.setUpdateTime(LocalDateTime.now());
        machineMapper.insert(machine);
        machine = machineMapper.getMachineByName(machine.getName());
        log.info("為id={}的機器{}新增tag", machine.getId(), machine.getName());
        //需要同步新增tag
        tagMapper.initialMachineTag(machine.getId());
    }

    @Override
    public Result updateTag(MachineTag tag) {

        List<Task> taskById = taskMapper.getTaskById(tag.getId());
        if(taskById.isEmpty()) {
            log.info("tag操作未受其他依賴資料影響");
            tag.setUpdateTime(LocalDateTime.now());
            tagMapper.updateMachineTagById(tag);
        } else {
            log.info("該機器已有任務排程，無法修改，task: {}", taskById);
            return Result.error("該機器已有任務排程，無法修改，task: " + taskById.toString());
        }
        return Result.success();
    }

    @Override
    public void updateMachine(Machine machine) {
        machine.setUpdateTime(LocalDateTime.now());
        machineMapper.updateMachine(machine);
    }

    @Override
    public void deleteMachine(Machine machine) {
        machineMapper.deleteMachineById(machine);
        //同步移除對應tag
        tagMapper.deleteTagByMachineId(machine.getId());
    }
}
