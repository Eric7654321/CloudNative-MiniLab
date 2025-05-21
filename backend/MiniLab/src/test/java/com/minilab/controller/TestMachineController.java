package com.minilab.controller;

import com.minilab.pojo.entity.Machine;
import com.minilab.pojo.entity.Result;
import com.minilab.pojo.vo.MachineVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class TestMachineController {

    @Autowired
    private MachineController machineController;

    @Test
    public void testSearchMachine() {
        log.info("測試機器查詢是否正常");
        Result result = machineController.machineSearch("114514");
        log.info("查詢結果: {}", result);
        Assertions.assertEquals(1, result.getCode());
    }

    @Test
    public void testInsertAndDeleteMachine() {
        log.info("測試新增與刪除機器操作");

        log.info("新增機器");
        Machine machine = new Machine();
        machine.setName("Test機器");
        machine.setMachineName("Machine-XYZ");
        machine.setGroup("114514");
        machine.setUsable(1);

        Result insertResult = machineController.insertMachine(machine);
        Assertions.assertEquals(1, insertResult.getCode());

        log.info("查詢剛剛新增的機器來取得 ID");
        Result searchResult = machineController.machineSearch("114514");
        @SuppressWarnings("unchecked")
        List<MachineVO> machines = (List<MachineVO>) searchResult.getData();
        Assertions.assertFalse(machines.isEmpty(), "找不到新增的機器");

        Machine toDelete = new Machine();
        BeanUtils.copyProperties(machines.get(machines.size() - 1), toDelete);  // 取最後一個當作新增的
        Result deleteResult = machineController.deleteMachine(toDelete);
        Assertions.assertEquals(1, deleteResult.getCode());

        machineController.deleteMachine(machine);
    }

    @Test
    public void testSearchAndUpdateMachine() {
        log.info("測試查詢與修改機器操作");

        Result result = machineController.machineSearch("114514");
        Assertions.assertEquals(1, result.getCode());

        @SuppressWarnings("unchecked")
        List<MachineVO> machines = (List<MachineVO>) result.getData();
        Assertions.assertFalse(machines.isEmpty(), "查無機器資料");
        log.info("查詢到機器: {}", machines);

        Machine machine = new Machine();
        BeanUtils.copyProperties(machines.get(0), machine);
        machine.setMachineName("管理員專用泡腳機");

        Result updateResult = machineController.updateMachine(machine);
        Assertions.assertEquals(1, updateResult.getCode());
    }
}
