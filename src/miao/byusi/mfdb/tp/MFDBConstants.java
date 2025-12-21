package miao.byusi.mfdb.tp;

public class MFDBConstants {
    public static final String ROOT_DIR_NAME = "MFDB-TP";
    public static final String NAME_FILE = "NAME";
    public static final String TEMP_FILE = "temp.tmp";
    public static final String RETURN_FILE = "mfdb-tp_return";
    public static final String LOG_DIR = "mfdb-tp_table";
    public static final String MASTERS_LOG = "mfdb_tp_masters.log";
    
    // 命令类型
    public static final String CMD_MASTER = "MASTER";
    public static final String CMD_TABLE = "TABLE";
    public static final String CMD_ADD = "ADD";
    public static final String CMD_FETCH = "FETCH";
    public static final String CMD_LIST = "LIST";
    
    // 分隔符
    public static final String COMMAND_SEPARATOR = "\\!";
    
    private MFDBConstants() {
        // 防止实例化
    }
}