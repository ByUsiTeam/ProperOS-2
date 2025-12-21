package miao.byusi.mfdb.tp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandParser {
    
    /**
     * 分割命令
     */
    public static List<String> splitCommands(String content) {
        // 使用 "\!" 作为分隔符
        String[] commands = content.split("\\\\!");
        return Arrays.asList(commands);
    }
    
    /**
     * 提取字符串
     */
    public static String extractString(String text, String startTag, String endTag) {
        if (text == null || startTag == null || endTag == null) {
            return "";
        }
        
        int startIndex = text.indexOf(startTag);
        if (startIndex == -1) {
            return "";
        }
        
        startIndex += startTag.length();
        int endIndex = text.indexOf(endTag, startIndex);
        if (endIndex == -1) {
            return "";
        }
        
        return text.substring(startIndex, endIndex);
    }
    
    /**
     * 判断是否包含
     */
    public static boolean contains(String text, String search) {
        return text != null && text.contains(search);
    }
    
    /**
     * 解析MASTER命令
     */
    public static Map<String, String> parseMasterCommand(String command) {
        Map<String, String> result = new HashMap<>();
        
        String masterContent = extractString(command, "MASTER{", "}END");
        if (masterContent.isEmpty()) {
            return result;
        }
        
        String name = extractString(masterContent, "NAME(", ")NAME_END");
        result.put("type", MFDBConstants.CMD_MASTER);
        result.put("name", name);
        result.put("content", masterContent);
        
        return result;
    }
    
    /**
     * 解析TABLE命令
     */
    public static Map<String, String> parseTableCommand(String command) {
        Map<String, String> result = new HashMap<>();
        
        String tableContent = extractString(command, "TABLE{", "}END");
        if (tableContent.isEmpty()) {
            return result;
        }
        
        String name = extractString(tableContent, "NAME(", ")NAME_END");
        String masterName = extractString(tableContent, "MASTRR(", ")MASTER_END");
        
        result.put("type", MFDBConstants.CMD_TABLE);
        result.put("name", name);
        result.put("master_name", masterName);
        result.put("content", tableContent);
        
        return result;
    }
    
    /**
     * 解析ADD命令
     */
    public static Map<String, String> parseAddCommand(String command) {
        Map<String, String> result = new HashMap<>();
        
        String addContent = extractString(command, "ADD{", "}END");
        if (addContent.isEmpty()) {
            return result;
        }
        
        String master = extractString(addContent, "MASTER(", ")MASTER_END");
        String table = extractString(addContent, "TABLE(", ")TABLE_END");
        String json = extractString(addContent, "JSON(", ")JSON_END");
        
        result.put("type", MFDBConstants.CMD_ADD);
        result.put("master", master);
        result.put("table", table);
        result.put("json", json);
        result.put("content", addContent);
        
        return result;
    }
    
    /**
     * 解析FETCH命令
     */
    public static Map<String, String> parseFetchCommand(String command) {
        Map<String, String> result = new HashMap<>();
        
        String fetchContent = extractString(command, "FETCH{", "}END");
        if (fetchContent.isEmpty()) {
            return result;
        }
        
        String master = extractString(fetchContent, "MASTER(", ")MASTER_END");
        String table = extractString(fetchContent, "TABLE(", ")TABLE_END");
        
        result.put("type", MFDBConstants.CMD_FETCH);
        result.put("master", master);
        result.put("table", table);
        result.put("content", fetchContent);
        
        return result;
    }
    
    /**
     * 检查是否是LIST命令
     */
    public static boolean isListCommand(String command) {
        return command != null && command.contains("LIST:/*");
    }
    
    /**
     * 验证参数是否有效
     */
    public static boolean validateParams(Map<String, String> params, String... requiredKeys) {
        for (String key : requiredKeys) {
            String value = params.get(key);
            if (value == null || value.isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 判断命令类型
     */
    public static String getCommandType(String command) {
        if (contains(command, "MASTER{")) {
            return MFDBConstants.CMD_MASTER;
        } else if (contains(command, "TABLE{")) {
            return MFDBConstants.CMD_TABLE;
        } else if (contains(command, "ADD{")) {
            return MFDBConstants.CMD_ADD;
        } else if (contains(command, "FETCH{")) {
            return MFDBConstants.CMD_FETCH;
        } else if (isListCommand(command)) {
            return MFDBConstants.CMD_LIST;
        }
        return "";
    }
}