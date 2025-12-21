package miao.byusi.mfdb.tp;

import org.json.JSONObject;
import org.json.JSONException;

public class JSONProcessor {
    
    /**
     * 解析JSON字符串
     */
    public static JSONObject parse(String jsonStr) throws MFDBException {
        try {
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                return new JSONObject();
            }
            return new JSONObject(jsonStr);
        } catch (JSONException e) {
            throw new MFDBException("JSON解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 设置JSON对象中的字段值
     */
    public static JSONObject setField(JSONObject jsonObj, String fieldPath, String value) throws MFDBException {
        try {
            // 尝试解析value为JSON
            JSONObject valueObj;
            try {
                valueObj = parse(value);
                jsonObj.put(fieldPath, valueObj);
            } catch (MFDBException e) {
                // 如果不是有效JSON，则作为字符串处理
                jsonObj.put(fieldPath, value);
            }
            return jsonObj;
        } catch (JSONException e) {
            throw new MFDBException("设置JSON字段失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取JSON对象中的字段值
     */
    public static String getField(JSONObject jsonObj, String fieldPath) {
        try {
            if (jsonObj.has(fieldPath)) {
                Object value = jsonObj.get(fieldPath);
                if (value instanceof JSONObject) {
                    return value.toString();
                } else if (value instanceof String) {
                    return (String) value;
                } else {
                    return String.valueOf(value);
                }
            }
            return "";
        } catch (JSONException e) {
            return "";
        }
    }
    
    /**
     * 创建基础表结构
     */
    public static String createTableStructure(String tableName) {
        try {
            JSONObject tableObj = new JSONObject();
            tableObj.put("name", tableName);
            tableObj.put("data", "");
            return tableObj.toString();
        } catch (JSONException e) {
            return "{\"name\":\"" + tableName + "\",\"data\":\"\"}";
        }
    }
    
    /**
     * 检查字符串是否为有效JSON
     */
    public static boolean isValidJSON(String jsonStr) {
        if (jsonStr == null || jsonStr.trim().isEmpty()) {
            return false;
        }
        try {
            new JSONObject(jsonStr);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
    
    /**
     * 美化JSON字符串
     */
    public static String prettyPrint(String jsonStr) throws MFDBException {
        try {
            JSONObject jsonObj = parse(jsonStr);
            return jsonObj.toString(2); // 缩进2个空格
        } catch (JSONException e) {
            throw new MFDBException("美化JSON失败", e);
        }
    }
    
    /**
     * 合并两个JSON对象
     */
    public static JSONObject merge(JSONObject obj1, JSONObject obj2) throws MFDBException {
        try {
            JSONObject result = new JSONObject(obj1.toString());
            
            // 遍历obj2的所有键
            java.util.Iterator<String> keys = obj2.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                Object value = obj2.get(key);
                result.put(key, value);
            }
            
            return result;
        } catch (JSONException e) {
            throw new MFDBException("合并JSON失败", e);
        }
    }
    
    /**
     * 从JSON对象中移除字段
     */
    public static JSONObject removeField(JSONObject jsonObj, String fieldPath) throws MFDBException {
        try {
            jsonObj.remove(fieldPath);
            return jsonObj;
        } catch (Exception e) {
            throw new MFDBException("移除字段失败", e);
        }
    }
    
    /**
     * 获取JSON对象的所有键
     */
    public static java.util.List<String> getKeys(JSONObject jsonObj) {
        java.util.List<String> keys = new java.util.ArrayList<>();
        if (jsonObj != null) {
            java.util.Iterator<String> iterator = jsonObj.keys();
            while (iterator.hasNext()) {
                keys.add(iterator.next());
            }
        }
        return keys;
    }
    
    /**
     * 检查JSON对象是否包含字段
     */
    public static boolean hasField(JSONObject jsonObj, String fieldPath) {
        return jsonObj != null && jsonObj.has(fieldPath);
    }
    
    /**
     * 转义JSON字符串
     */
    public static String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }
    
    /**
     * 解析JSON数组字符串
     */
    public static org.json.JSONArray parseArray(String jsonArrayStr) throws MFDBException {
        try {
            if (jsonArrayStr == null || jsonArrayStr.trim().isEmpty()) {
                return new org.json.JSONArray();
            }
            return new org.json.JSONArray(jsonArrayStr);
        } catch (JSONException e) {
            throw new MFDBException("JSON数组解析失败", e);
        }
    }
    
    /**
     * 创建JSON数组字符串
     */
    public static String createJsonArray(java.util.List<String> items) {
        try {
            org.json.JSONArray jsonArray = new org.json.JSONArray();
            for (String item : items) {
                jsonArray.put(item);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}