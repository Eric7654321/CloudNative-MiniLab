package com.minilab.service.impl;

import com.minilab.mapper.MachineMapper;
import com.minilab.mapper.TagMapper;
import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.MachineTag;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MachineServiceImpl implements MachineService {

    @Autowired
    MachineMapper machineMapper;
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
    public void insert(Machine machine) {
        machine.setUpdateTime(LocalDateTime.now());
        machineMapper.insert(machine);
        //需要同步新增tag
        tagMapper.initialMachineTag(machine.getId());
    }

    @Override
    public void updateTag(MachineTag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.updateMachineTagById(tag);
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
