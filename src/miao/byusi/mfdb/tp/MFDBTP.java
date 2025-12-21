package miao.byusi.mfdb.tp;

import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.Map;

public class MFDBTP {
    private static final String TAG = "MFDBTP";
    
    private Context context;
    private FileDBManager dbManager;
    private String fetchReturn;
    
    /**
     * 构造函数
     */
    public MFDBTP(Context context) {
        this.context = context.getApplicationContext();
        this.dbManager = new FileDBManager(this.context);
    }
    
    /**
     * 主函数 - 执行命令
     */
    public void code(String content) throws MFDBException {
        // 检查输入是否为空
        if (content == null || content.trim().isEmpty()) {
            Log.e(TAG, "[ERROR] 错误，传入数据为空");
            throw new MFDBException("传入数据为空");
        }
        
        Log.d(TAG, "开始执行命令: " + content);
        
        try {
            // 分割命令
            List<String> commands = CommandParser.splitCommands(content);
            
            // 检查分割结果
            if (commands == null || commands.isEmpty()) {
                Log.e(TAG, "[ERROR] 指令解析失败了，请重新尝试吧");
                collapsed();
                return;
            }
            
            // 执行每个命令
            for (String command : commands) {
                processCommand(command.trim());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "[ERROR] 执行过程中出现异常: " + e.getMessage());
            throw new MFDBException("命令执行失败", e);
        }
    }
    
    /**
     * 处理单个命令
     */
    private void processCommand(String command) throws MFDBException {
        Log.d(TAG, "传入待执行命令 ➤ " + command);
        
        String commandType = CommandParser.getCommandType(command);
        
        switch (commandType) {
            case MFDBConstants.CMD_MASTER:
                processMasterCommand(command);
                break;
            case MFDBConstants.CMD_TABLE:
                processTableCommand(command);
                break;
            case MFDBConstants.CMD_ADD:
                processAddCommand(command);
                break;
            case MFDBConstants.CMD_FETCH:
                processFetchCommand(command);
                break;
            case MFDBConstants.CMD_LIST:
                processListCommand();
                break;
            default:
                Log.e(TAG, "[ERROR] 未知指令");
                throw new MFDBException("未知指令: " + command);
        }
    }
    
    /**
     * 处理MASTER命令
     */
    private void processMasterCommand(String command) throws MFDBException {
        Map<String, String> params = CommandParser.parseMasterCommand(command);
        
        // 验证参数
        if (!CommandParser.validateParams(params, "name", "content")) {
            Log.e(TAG, "[ERROR] 操作失败");
            throw new MFDBException("MASTER命令参数不完整");
        }
        
        String name = params.get("name");
        
        // 创建数据库
        boolean success = dbManager.createMaster(name);
        
        if (success) {
            Log.i(TAG, "操作成功 - 创建数据库: " + name);
        } else {
            Log.e(TAG, "[ERROR] 操作失败");
            throw new MFDBException("MASTER操作失败: " + name);
        }
    }
    
    /**
     * 处理TABLE命令
     */
    private void processTableCommand(String command) throws MFDBException {
        Map<String, String> params = CommandParser.parseTableCommand(command);
        
        // 验证参数
        if (!CommandParser.validateParams(params, "name", "master_name", "content")) {
            Log.e(TAG, "[ERROR] 操作失败");
            throw new MFDBException("TABLE命令参数不完整");
        }
        
        String name = params.get("name");
        String masterName = params.get("master_name");
        
        // 创建表
        boolean success = dbManager.createTable(masterName, name);
        
        if (success) {
            Log.i(TAG, "操作成功 - 创建表: " + masterName + "/" + name);
        } else {
            Log.e(TAG, "[ERROR] 操作失败");
            throw new MFDBException("TABLE操作失败: " + masterName + "/" + name);
        }
    }
    
    /**
     * 处理ADD命令
     */
    private void processAddCommand(String command) throws MFDBException {
        Map<String, String> params = CommandParser.parseAddCommand(command);
        
        // 验证参数
        if (!CommandParser.validateParams(params, "master", "table", "json", "content")) {
            Log.e(TAG, "[ERROR] 执行出错了");
            throw new MFDBException("ADD命令参数不完整");
        }
        
        String master = params.get("master");
        String table = params.get("table");
        String json = params.get("json");
        
        // 添加数据
        boolean success = dbManager.addData(master, table, json);
        
        if (success) {
            Log.i(TAG, "操作完成 - 添加数据到: " + master + "/" + table);
        } else {
            Log.e(TAG, "[ERROR] 执行出错了");
            throw new MFDBException("ADD操作失败: " + master + "/" + table);
        }
    }
    
    /**
     * 处理FETCH命令
     */
    private void processFetchCommand(String command) throws MFDBException {
        Map<String, String> params = CommandParser.parseFetchCommand(command);
        
        // 验证参数
        if (!CommandParser.validateParams(params, "master", "table", "content")) {
            Log.e(TAG, "[ERROR] 操作失败");
            throw new MFDBException("FETCH命令参数不完整");
        }
        
        String master = params.get("master");
        String table = params.get("table");
        
        try {
            // 获取数据
            String data = dbManager.fetchData(master, table);
            
            if (data != null) {
                // 保存返回结果
                this.fetchReturn = data;
                
                // 写入返回文件
                dbManager.writeReturnResult(data);
                
                Log.i(TAG, "操作完成 - 获取数据从: " + master + "/" + table);
                Log.d(TAG, "数据内容: " + data);
            } else {
                Log.e(TAG, "[ERROR] 数据库损坏了");
                throw new MFDBException("数据库损坏: " + master + "/" + table);
            }
        } catch (MFDBException e) {
            Log.e(TAG, "[ERROR] 操作失败: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * 处理LIST命令
     */
    private void processListCommand() throws MFDBException {
        // 获取所有数据库
        List<String> masters = dbManager.listMasters();
        
        // 记录日志
        dbManager.logListOperation(masters);
        
        Log.i(TAG, "操作完成 - 列出数据库:");
        for (String master : masters) {
            Log.i(TAG, "  • " + master);
        }
    }
    
    /**
     * 崩溃函数
     */
    public void collapsed() {
        Log.e(TAG, "系统崩溃...");
        // 递归调用导致崩溃
        collapsed();
    }
    
    /**
     * 列表函数
     */
    public void list() throws MFDBException {
        processListCommand();
    }
    
    /**
     * 获取FETCH返回结果
     */
    public String getFetchReturn() {
        return fetchReturn;
    }
    
    /**
     * 读取返回文件
     */
    public String readReturnResult() throws MFDBException {
        return dbManager.readReturnResult();
    }
    
    /**
     * 列出所有数据库（返回列表）
     */
    public List<String> listMasters() throws MFDBException {
        return dbManager.listMasters();
    }
    
    /**
     * 直接创建数据库（便捷方法）
     */
    public void createMasterDirect(String masterName) throws MFDBException {
        String command = "MASTER{NAME(" + masterName + ")NAME_END}END";
        code(command);
    }
    
    /**
     * 直接创建表（便捷方法）
     */
    public void createTableDirect(String masterName, String tableName) throws MFDBException {
        String command = "TABLE{NAME(" + tableName + ")NAME_ENDMASTRR(" + masterName + ")MASTER_END}END";
        code(command);
    }
    
    /**
     * 直接添加数据（便捷方法）
     */
    public void addDataDirect(String masterName, String tableName, String jsonData) throws MFDBException {
        String command = "ADD{MASTER(" + masterName + ")MASTER_ENDTABLE(" + tableName + ")TABLE_ENDJSON(" + jsonData + ")JSON_END}END";
        code(command);
    }
    
    /**
     * 直接获取数据（便捷方法）
     */
    public String fetchDataDirect(String masterName, String tableName) throws MFDBException {
        String command = "FETCH{MASTER(" + masterName + ")MASTER_ENDTABLE(" + tableName + ")TABLE_END}END";
        code(command);
        return getFetchReturn();
    }
}