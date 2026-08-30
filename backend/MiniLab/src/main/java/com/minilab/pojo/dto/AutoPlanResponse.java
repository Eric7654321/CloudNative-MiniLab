package com.minilab.pojo.dto;

import com.minilab.pojo.entity.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 自動排程的結果。排得進去的變成待確認的 Task，排不進去的附上原因原樣退回，
 * 讓組長自己決定要改時段、改技能還是放棄——系統不替他做這個決定。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoPlanResponse {
    List<Task> assigned;
    List<Rejected> rejected;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rejected {
        String requestId;
        String reason;
    }
}
