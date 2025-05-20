package com.minilab.service;

import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.EmpTag;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.EmpVO;

import java.util.List;

public interface EmpService {
    Emp login(Emp emp);

    List<EmpVO> getEmpByGroupId(String groupId);

    void insert(Emp emp);

    Result updateTag(EmpTag tag);

    void updateEmp(Emp emp);

    void deleteEmp(Emp emp);

    Emp seleteEmpByUsername(String username);
}
