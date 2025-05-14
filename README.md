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