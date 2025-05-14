package com.minilab.service;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.vo.MachineVO;

import java.util.List;

public interface MachineService {
    List<MachineVO> getMachineByGroupId(String groupId);
}
