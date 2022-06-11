# L1JTW - 3.0c 懷舊社群版

準備事項:
1. JDK 8 (windows/linux)
2. Eclipse Version: Neon.3 (4.6.3)
3. git

編譯步驟:
1. 開啟L1J workspace
2. 設定 Window → Preference → Java → Installed JREs → Add JDK path and select it
3. 在專案的build.xml點右鍵 → Run As.. → Ant Build
4. 已編譯的l1jserver.jar就可以拿來用了

如果要在本機端測試，你需要:
1. Lineage3.0c客戶端
2. 登入器(改ip)
3. MySQL server (db資料夾內有l1jdb.sql，需要先匯入)