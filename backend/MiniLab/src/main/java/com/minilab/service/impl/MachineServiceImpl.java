package com.minilab.service.impl;

import com.minilab.mapper.MachineMapper;
import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.pojo.vo.MachineVO;
import com.minilab.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MachineServiceImpl implements MachineService {

    @Autowired
    MachineMapper machineMapper;

    @Override
    public List<MachineVO> getMachineByGroupId(String groupId) {
        List<MachineVO> machines = machineMapper.getMachineByGroupId(groupId);
        log.info("已查詢到machines: {}, groupId={}, 接著查詢tags", machines, groupId);
        for(MachineVO machine : machines) {
            machine.setTags(machineMapper.setTagsByUserId(machine.getId()));
        }
        return machines;
    }
}
