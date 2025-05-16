package com.minilab.service.impl;

import com.minilab.mapper.EmpMapper;
import com.minilab.mapper.TagMapper;
import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.vo.EmpVO;
import com.minilab.service.EmpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class EmpServiceImpl implements EmpService {
    @Autowired
    EmpMapper empMapper;
    @Autowired
    TagMapper tagMapper;

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

    @Override
    public void insert(Emp emp) {
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.insert(emp);
        //需要同步新增tag
        tagMapper.initialEmpTag(emp.getId());
    }

    @Override
    public void updateTag(EmpTag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.updateEmpTagById(tag);
    }

    @Override
    public void updateEmp(Emp emp) {
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.updateEmp(emp);
    }

    @Override
    @Transactional
    public void deleteEmp(Emp emp) {
        empMapper.deleteEmpById(emp);
        //同步移除對應tag
        tagMapper.deleteTagByEmpId(emp.getId());
    }
}
