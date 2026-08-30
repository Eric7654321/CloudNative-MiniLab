package com.minilab.service;

import com.minilab.pojo.dto.AutoPlanRequest;
import com.minilab.pojo.entity.Result;

import java.util.List;

public interface AutoScheduleService {
    /** 依 group 內目前的人力物力與既有任務，替這批需求算出建議指派。不寫入資料庫。 */
    Result plan(String group, List<AutoPlanRequest> requests);
}
