package com.minilab.service;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.MachineTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.MachineVO;

import java.util.List;

public interface MachineService {
    List<MachineVO> getMachineByGroupId(String groupId);

    void insert(Machine machine);

    Result updateTag(MachineTag tag);

    void updateMachine(Machine machine);

    void deleteMachine(Machine machine);
}
