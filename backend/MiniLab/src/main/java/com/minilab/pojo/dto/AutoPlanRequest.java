package com.minilab.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 一筆還沒有指派人力物力的任務需求，交給自動排程去配。
 *
 * 與 {@code Task} 分開是因為這裡刻意沒有 emp、machine 與確切時段：
 * 那三樣正是自動排程要算出來的東西，讓它們在請求裡存在只會招來「前端先填一半」的呼叫方式。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoPlanRequest {
    /** 前端用來把回傳的指派對回自己那筆草稿；後端不解讀內容。 */
    String requestId;
    /** 任務所需技能，多個以逗號分隔，與 Task.tag 同格式。 */
    String tag;
    /** 這件事要做多久（分鐘）。 */
    Integer durationMinutes;
    /** 最早不早於這個時間開始；未填視為現在。 */
    LocalDateTime earliestStart;
    /** 最晚必須結束的時間；未填表示沒有期限。 */
    LocalDateTime deadline;
    /** 這個任務需要幾台機器，未填視為 1。 */
    Integer machineCount;
    String description;
    String group;
}
