package com.minilab.controller;


import com.minilab.pojo.vo.EmpVO;
import lombok.extern.slf4j.Slf4j;
import com.minilab.pojo.entity.Emp;
import com.minilab.pojo.entity.Result;
import com.minilab.service.EmpService;
import com.minilab.utils.JwtUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class LoginController {

    @Autowired
    private EmpService empService;

    @PostMapping("/login")
    public Result login(@RequestBody Emp emp){
        log.info("員工登入:{}",emp);
        Emp e = empService.login(emp);
        EmpVO empVO = new EmpVO();

        //登入檢查
        if(e != null){
            Map<String, Object> claims = new HashMap<>();
            claims.put("id",e.getId());
            claims.put("name",e.getName());
            claims.put("username", e.getUsername());
            claims.put("usable", e.getUsable());
            claims.put("group", e.getGroup());
            claims.put("role", e.getRole());
            String s = empService.selectEmpTagsByUsername(e.getUsername());
            claims.put("tags", s);

            String jwt = JwtUtils.generateJwt(claims);
            BeanUtils.copyProperties(e, empVO);
            empVO.setTags(s);
            empVO.setJwt(jwt);

            return Result.success(empVO);
        }

        return e != null ?Result.success():Result.error("帳號或密碼錯誤!");
    }
}
