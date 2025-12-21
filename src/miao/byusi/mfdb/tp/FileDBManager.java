package miao.byusi.mfdb.tp;

import android.content.Context;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class FileDBManager {
    private Context context;
    private File rootDir;
    private File mfdbRoot;
    
    public FileDBManager(Context context) {
        this.context = context;
        // 获取应用的文件目录（对应 iApp 的 fdir("$",root_dir)）
        this.rootDir = context.getFilesDir().getParentFile(); // 移除/files
        this.mfdbRoot = new File(rootDir, MFDBConstants.ROOT_DIR_NAME);
    }
    
    /**
     * 获取MFDB根目录
     */
    public File getMFDBRoot() {
        return mfdbRoot;
    }
    
    /**
     * 创建数据库（MASTER命令）
     */
    public boolean createMaster(String masterName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            
            // 创建目录（使用临时文件技巧）
            File tempFile = new File(masterDir, MFDBConstants.TEMP_FILE);
            if (!tempFile.getParentFile().exists()) {
                if (!tempFile.getParentFile().mkdirs()) {
                    throw new IOException("无法创建目录: " + masterDir.getAbsolutePath());
                }
            }
            
            // 创建临时文件
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write("");
            }
            
            // 删除临时文件
            if (!tempFile.delete()) {
                throw new IOException("无法删除临时文件: " + tempFile.getAbsolutePath());
            }
            
            // 创建NAME文件
            File nameFile = new File(masterDir, MFDBConstants.NAME_FILE);
            try (FileWriter writer = new FileWriter(nameFile)) {
                writer.write(masterName);
            }
            
            return masterDir.exists() && nameFile.exists();
        } catch (IOException e) {
            throw new MFDBException("创建数据库失败: " + masterName, e);
        }
    }
    
    /**
     * 创建表（TABLE命令）
     */
    public boolean createTable(String masterName, String tableName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            File tableFile = new File(masterDir, tableName + ".json");
            
            String tableJson = JSONProcessor.createTableStructure(tableName);
            
            try (FileWriter writer = new FileWriter(tableFile)) {
                writer.write(tableJson);
            }
            
            return tableFile.exists();
        } catch (IOException e) {
            throw new MFDBException("创建表失败: " + masterName + "/" + tableName, e);
        }
    }
    
    /**
     * 添加/更新数据（ADD命令）
     */
    public boolean addData(String masterName, String tableName, String jsonData) throws MFDBException {
        try {
            File tableFile = new File(mfdbRoot, masterName + "/" + tableName + ".json");
            
            // 读取现有数据
            String existingData;
            if (tableFile.exists()) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                }
                existingData = content.toString();
            } else {
                existingData = JSONProcessor.createTableStructure(tableName);
            }
            
            // 解析JSON
            JSONObject jsonObj = JSONProcessor.parse(existingData);
            
            // 设置data字段
            jsonObj = JSONProcessor.setField(jsonObj, "data", jsonData);
            
            // 写回文件
            try (FileWriter writer = new FileWriter(tableFile)) {
                writer.write(jsonObj.toString());
            }
            
            return true;
        } catch (IOException e) {
            throw new MFDBException("添加数据失败: " + masterName + "/" + tableName, e);
        }
    }
    
    /**
     * 获取数据（FETCH命令）
     */
    public String fetchData(String masterName, String tableName) throws MFDBException {
        try {
            File tableFile = new File(mfdbRoot, masterName + "/" + tableName + ".json");
            
            if (!tableFile.exists()) {
                throw new MFDBException("表不存在: " + masterName + "/" + tableName);
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(tableFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            String tableData = content.toString();
            JSONObject jsonObj = JSONProcessor.parse(tableData);
            
            // 获取data字段
            return JSONProcessor.getField(jsonObj, "data");
        } catch (IOException e) {
            throw new MFDBException("读取数据失败: " + masterName + "/" + tableName, e);
        }
    }
    
    /**
     * 列出所有数据库（LIST命令）
     */
    public List<String> listMasters() throws MFDBException {
        try {
            if (!mfdbRoot.exists()) {
                return new ArrayList<>();
            }
            
            File[] files = mfdbRoot.listFiles(File::isDirectory);
            List<String> masters = new ArrayList<>();
            
            if (files != null) {
                for (File file : files) {
                    masters.add(file.getName());
                }
            }
            
            return masters;
        } catch (Exception e) {
            throw new MFDBException("列出数据库失败", e);
        }
    }
    
    /**
     * 列出数据库中的所有表
     */
    public List<String> listTables(String masterName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            File[] files = masterDir.listFiles((dir, name) -> name.endsWith(".json"));
            List<String> tables = new ArrayList<>();
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    // 移除.json后缀
                    if (name.endsWith(".json")) {
                        tables.add(name.substring(0, name.length() - 5));
                    }
                }
            }
            
            return tables;
        } catch (Exception e) {
            throw new MFDBException("列出表失败: " + masterName, e);
        }
    }
    
    /**
     * 记录日志
     */
    public void logListOperation(List<String> masters) throws MFDBException {
        try {
            File logDir = new File(rootDir, MFDBConstants.LOG_DIR);
            if (!logDir.exists()) {
                if (!logDir.mkdirs()) {
                    throw new IOException("无法创建日志目录");
                }
            }
            
            // 生成时间戳文件名
            long timestamp = System.currentTimeMillis();
            File logFile = new File(logDir, timestamp + ".log");
            
            // 构建日志内容
            StringBuilder logContent = new StringBuilder();
            for (String master : masters) {
                logContent.append(master).append("\n");
            }
            
            // 写入时间戳日志文件
            try (FileWriter writer = new FileWriter(logFile)) {
                writer.write(logContent.toString());
            }
            
            // 同时写入固定文件
            File fixedLogFile = new File(rootDir, MFDBConstants.MASTERS_LOG);
            try (FileWriter writer = new FileWriter(fixedLogFile)) {
                writer.write(logContent.toString());
            }
        } catch (IOException e) {
            throw new MFDBException("记录日志失败", e);
        }
    }
    
    /**
     * 写入返回结果
     */
    public void writeReturnResult(String data) throws MFDBException {
        try {
            File returnFile = new File(rootDir, MFDBConstants.RETURN_FILE);
            try (FileWriter writer = new FileWriter(returnFile)) {
                writer.write(data);
            }
        } catch (IOException e) {
            throw new MFDBException("写入返回结果失败", e);
        }
    }
    
    /**
     * 读取返回结果
     */
    public String readReturnResult() throws MFDBException {
        try {
            File returnFile = new File(rootDir, MFDBConstants.RETURN_FILE);
            if (!returnFile.exists()) {
                return "";
            }
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(returnFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            }
            
            return content.toString();
        } catch (IOException e) {
            throw new MFDBException("读取返回结果失败", e);
        }
    }
    
    /**
     * 删除数据库
     */
    public boolean deleteMaster(String masterName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            return deleteDirectory(masterDir);
        } catch (Exception e) {
            throw new MFDBException("删除数据库失败: " + masterName, e);
        }
    }
    
    /**
     * 删除表
     */
    public boolean deleteTable(String masterName, String tableName) throws MFDBException {
        try {
            File tableFile = new File(mfdbRoot, masterName + "/" + tableName + ".json");
            if (!tableFile.exists()) {
                throw new MFDBException("表不存在: " + masterName + "/" + tableName);
            }
            
            return tableFile.delete();
        } catch (Exception e) {
            throw new MFDBException("删除表失败: " + masterName + "/" + tableName, e);
        }
    }
    
    /**
     * 删除文件
     */
    public boolean deleteFile(String path) {
        try {
            File file = new File(rootDir, path);
            return file.delete();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public boolean fileExists(String path) {
        File file = new File(rootDir, path);
        return file.exists();
    }
    
    /**
     * 检查数据库是否存在
     */
    public boolean masterExists(String masterName) {
        File masterDir = new File(mfdbRoot, masterName);
        return masterDir.exists() && masterDir.isDirectory();
    }
    
    /**
     * 检查表是否存在
     */
    public boolean tableExists(String masterName, String tableName) {
        File tableFile = new File(mfdbRoot, masterName + "/" + tableName + ".json");
        return tableFile.exists();
    }
    
    /**
     * 递归删除目录
     */
    private boolean deleteDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            return directory.delete();
        }
        return false;
    }
    
    /**
     * 获取数据库路径
     */
    public String getMasterPath(String masterName) {
        File masterDir = new File(mfdbRoot, masterName);
        return masterDir.getAbsolutePath();
    }
    
    /**
     * 获取表路径
     */
    public String getTablePath(String masterName, String tableName) {
        File tableFile = new File(mfdbRoot, masterName + "/" + tableName + ".json");
        return tableFile.getAbsolutePath();
    }
    
    /**
     * 清理临时文件
     */
    public void cleanupTempFiles() throws MFDBException {
        try {
            // 清理所有数据库目录中的temp.tmp文件
            if (mfdbRoot.exists() && mfdbRoot.isDirectory()) {
                File[] masters = mfdbRoot.listFiles(File::isDirectory);
                if (masters != null) {
                    for (File master : masters) {
                        File tempFile = new File(master, MFDBConstants.TEMP_FILE);
                        if (tempFile.exists()) {
                            tempFile.delete();
                        }
                    }
                }
            }
            
            // 清理旧日志文件（保留最近10个）
            File logDir = new File(rootDir, MFDBConstants.LOG_DIR);
            if (logDir.exists() && logDir.isDirectory()) {
                File[] logFiles = logDir.listFiles();
                if (logFiles != null && logFiles.length > 10) {
                    // 按修改时间排序
                    java.util.Arrays.sort(logFiles, (f1, f2) -> 
                        Long.compare(f2.lastModified(), f1.lastModified()));
                    
                    // 删除超过10个的旧文件
                    for (int i = 10; i < logFiles.length; i++) {
                        logFiles[i].delete();
                    }
                }
            }
        } catch (Exception e) {
            throw new MFDBException("清理临时文件失败", e);
        }
    }
    
    /**
     * 备份数据库
     */
    public boolean backupMaster(String masterName, String backupPath) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            File backupDir = new File(backupPath, masterName + "_backup_" + System.currentTimeMillis());
            if (!backupDir.mkdirs()) {
                throw new IOException("无法创建备份目录: " + backupDir.getAbsolutePath());
            }
            
            return copyDirectory(masterDir, backupDir);
        } catch (IOException e) {
            throw new MFDBException("备份数据库失败: " + masterName, e);
        }
    }
    
    /**
     * 恢复数据库
     */
    public boolean restoreMaster(String masterName, String backupPath) throws MFDBException {
        try {
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                throw new MFDBException("备份目录不存在: " + backupPath);
            }
            
            // 先删除现有数据库
            File masterDir = new File(mfdbRoot, masterName);
            if (masterDir.exists()) {
                if (!deleteDirectory(masterDir)) {
                    throw new IOException("无法删除现有数据库");
                }
            }
            
            // 从备份恢复
            return copyDirectory(backupDir, masterDir);
        } catch (IOException e) {
            throw new MFDBException("恢复数据库失败: " + masterName, e);
        }
    }
    
    /**
     * 复制目录
     */
    private boolean copyDirectory(File source, File destination) throws IOException {
        if (source.isDirectory()) {
            if (!destination.exists() && !destination.mkdirs()) {
                return false;
            }
            
            String[] files = source.list();
            if (files == null) {
                return true;
            }
            
            for (String file : files) {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);
                if (!copyDirectory(srcFile, destFile)) {
                    return false;
                }
            }
            return true;
        } else {
            // 复制文件
            try (BufferedReader reader = new BufferedReader(new FileReader(source));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(destination))) {
                
                char[] buffer = new char[1024];
                int length;
                while ((length = reader.read(buffer)) > 0) {
                    writer.write(buffer, 0, length);
                }
            }
            return true;
        }
    }
    
    /**
     * 获取数据库大小（字节）
     */
    public long getMasterSize(String masterName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            return getDirectorySize(masterDir);
        } catch (Exception e) {
            throw new MFDBException("获取数据库大小失败: " + masterName, e);
        }
    }
    
    /**
     * 获取总数据库大小（字节）
     */
    public long getTotalSize() {
        if (!mfdbRoot.exists()) {
            return 0;
        }
        
        return getDirectorySize(mfdbRoot);
    }
    
    /**
     * 计算目录大小
     */
    private long getDirectorySize(File directory) {
        long size = 0;
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirectorySize(file);
                    }
                }
            }
        }
        return size;
    }
    
    /**
     * 验证数据库完整性
     */
    public boolean validateMaster(String masterName) throws MFDBException {
        try {
            File masterDir = new File(mfdbRoot, masterName);
            if (!masterDir.exists()) {
                throw new MFDBException("数据库不存在: " + masterName);
            }
            
            // 检查NAME文件
            File nameFile = new File(masterDir, MFDBConstants.NAME_FILE);
            if (!nameFile.exists()) {
                return false;
            }
            
            // 读取NAME文件内容
            StringBuilder nameContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(nameFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    nameContent.append(line);
                }
            }
            
            // 验证名称是否匹配
            if (!nameContent.toString().equals(masterName)) {
                return false;
            }
            
            // 验证所有表文件
            File[] tableFiles = masterDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (tableFiles != null) {
                for (File tableFile : tableFiles) {
                    try {
                        String content = readFileContent(tableFile);
                        JSONObject jsonObj = JSONProcessor.parse(content);
                        String name = JSONProcessor.getField(jsonObj, "name");
                        if (name == null || name.isEmpty()) {
                            return false;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            
            return true;
        } catch (IOException e) {
            throw new MFDBException("验证数据库失败: " + masterName, e);
        }
    }
    
    /**
     * 读取文件内容
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }
}