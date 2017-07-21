package utils;

/**
 * Created by zhengyu on 21/07/2017.
 */
public class ResponseException extends Exception {

    private int mErrorCode;

    public ResponseException(int errorCode) {
        mErrorCode = errorCode;
    }
}
