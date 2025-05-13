package com.minilab.service.impl;

import com.minilab.mapper.EmpMapper;
import com.minilab.pojo.entity.Emp;
import com.minilab.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmpServiceImpl implements EmpService {
    @Autowired
    EmpMapper empMapper;

    @Override
    public Emp login(Emp emp) {
        return empMapper.getByUsernameAndPassword(emp.getUsername(), emp.getPassword());
    }
}
