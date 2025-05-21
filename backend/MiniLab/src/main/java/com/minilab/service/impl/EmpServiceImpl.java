package com.minilab.service.impl;

import com.minilab.mapper.EmpMapper;
import com.minilab.mapper.TagMapper;
import com.minilab.mapper.TaskMapper;
import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.entity.Task;
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
    @Autowired
    private TaskMapper taskMapper;

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
    @Transactional
    public void insert(Emp emp) {
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.insert(emp);
        emp = empMapper.getEmpByUsername(emp.getUsername());
        log.info("為id={}的使用者{}新增tag", emp.getId(), emp.getUsername());
        //需要同步新增tag
        tagMapper.initialEmpTag(emp.getId());
    }

    @Override
    public String selectEmpTagsByUsername(String username) {
        Emp emp = empMapper.getEmpByUsername(username);
        String s = tagMapper.selectEmpTagsByEmpId(emp.getId());
        log.info("查詢user: {}, tags: {}", username, s);
        return s;
    }

    @Override
    public Result updateTag(EmpTag tag) {
        List<Task> taskById = taskMapper.getTaskById(tag.getEmpId());
        if(taskById.isEmpty()) {
            log.info("tag操作未受其他依賴資料影響");
            tag.setUpdateTime(LocalDateTime.now());
            tagMapper.updateEmpTagById(tag);
        } else {
            log.info("該使用者已有任務排程，無法修改，task: {}", taskById);
            return Result.error("該使用者已有任務排程，無法修改，task: " + taskById.toString());
        }
        return Result.success();

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

    @Override
    public Emp seleteEmpByUsername(String username) {
        Emp emp = empMapper.getEmpByUsername(username);
        return emp;
    }
}
