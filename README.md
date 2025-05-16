# CloudNative-MiniLab
雲原生軟體開發與最佳實踐期末專題

[HackMD 連結](https://hackmd.io/BB4n_xctTsus2xEmPvc1dQ?view)

## 目前已Implement的API
| Method | Request    | 描述                                                                                              | provide    | return |
| ------ | --- | ------------------------------------------------------------------------------------------------- | ---------- | ------ |
| Post   | /login    | 查詢登入者是否存在，並且提供role給前端決定要跳轉到哪個page，用於登入後的驗證 | {  "username": "your_user_name",  "password": "your_password"} | EmpVO   |

| Method | Request    | 描述                                 | provide      |  return   |
| ------ | --- | ------------------------------------ | ------------ |:---------:|
| Get    | /emp/search/{groupid}    | 查詢所有同個組別下的工人狀態         | groupid      | 工人list  |
| Get    | /task/search/{groupid}     | 查詢所有同個組別下的任務排程         | groupid      | 任務list  |
| Get    | /machine/search/{groupid}     | 查詢所有同個組別下的機器狀態         | groupid      | 機器list  |
| Post   | /schedule/task     | #❌手動為某個成員排定任務               | groupid      |   null    |
| Post   | /schedule/auto/task    | #❌請求server為group成員/機器排定任務 | groupid      | task list |
| Post   | /schedule/auto/ack    | #❌對於自動排程的任務進行確認           | task list    |   null    |
| Delete | /schedule/task/delete    | 刪除任務                             | task         |   null    |
| Put    | /schedule/task/update    | 修改任務                             | task         |   null    |
| Post   | /emp/insert    | 新增一名工人                         | 工人/groupid |   null    |
| Put    | /emp/tag/update    | 修改工人tag內容                      | tag list     |   null    |
| Put    | /emp/update    | 修改工人資訊                         | 工人         |   null    |
| Delete | /emp/delete    | 刪除工人                             | 工人         |   null    |
| Post   | /machine/insert    | 新增一台機器                         | 工人/groupid |   null    |
| Put    | /machine/tag/update    | 修改機器tag內容                      | tag list     |   null    |
| Put    | /machine/update    | 修改機器資訊                         | 工人         |   null    |
| Delete | /machine/delete    | 刪除機器                             | 工人         |   null    |

| Method | Request    | 描述                   | provide | return    |
| ------ | --- | ---------------------- | ------- | --------- |
| Get    | /task/check/today/{id}    | 查詢自己今日的工作排程 | 工人id    | task list |

| Method | Request    | 描述     | provide | return |
| ------ | --- | -------- | ------- | ------ |
| Post   | /task/msg/send    | 回報任務 | Msg     | null   |

| Method | Request    | 描述             | provide | return   |
| ------ | --- | ---------------- | ------- | -------- |
| Get    | /task/msg/get/{groupid}    | 獲取所有同組訊息 | groupid | msg list |

| Method | Request    | 描述                   | provide | return    |
| ------ | --- | ---------------------- | ------- | --------- |
| Get    | /task/check/weeks/{id}    | 獲取兩個禮拜內所有排程 | user    | task list |

