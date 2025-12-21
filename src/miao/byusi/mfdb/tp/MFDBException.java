package miao.byusi.mfdb.tp;

public class MFDBException extends Exception {
    public MFDBException(String message) {
        super(message);
    }
    
    public MFDBException(String message, Throwable cause) {
        super(message, cause);
    }
}