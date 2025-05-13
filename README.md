# CloudNative-MiniLab
雲原生軟體開發與最佳實踐期末專題

[HackMD 連結](https://hackmd.io/BB4n_xctTsus2xEmPvc1dQ?view)

## 目前已Implement的API
| Method | Request    | 描述                                                                                              | provide    | return |
| ------ | --- | ------------------------------------------------------------------------------------------------- | ---------- | ------ |
| Post   | /login    | 查詢登入者是否存在，並且藉由相對應的role決定要跳轉到哪個page，head裡面包含一jwt，用於登入後的驗證 | {  "username": "your_user_name",  "password": "your_password"} | jwt   |
