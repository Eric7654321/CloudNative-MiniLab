package com.minilab.service.impl;

import com.minilab.mapper.EmpMapper;
import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmpServiceImpl implements EmpService {
    @Autowired
    EmpMapper empMapper;

    @Override
    public Emp login(Emp emp) {
        return empMapper.getByUsernameAndPassword(emp.getUsername(), emp.getPassword());
    }

    @Override
    public List<EmpVO> getEmpByGroupId(String groupId) {
        List<EmpVO> EmpVos = empMapper.getEmpByGroupId(groupId);
        log.info("已查詢到Emps: {}, groupId={}, 接著查詢tags", EmpVos, groupId);
        for(EmpVO empVo : EmpVos) {
            empVo.setTags(empMapper.setTagsByUserId(empVo.getId()));
        }
        return EmpVos;
    }
}
