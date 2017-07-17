package dliehr.com.fahrtenbuch;

/**
 * Created by Dominik on 24.08.16.
 */
public class Error {
    private int ErrorCode = 0;
    private String ErrorText = "";

    public Error(int ecode, String etext) {
        this.ErrorCode = ecode;
        this.ErrorText = etext;
    }

    public String getErrorText() { return this.ErrorText; }
    public int getErrorCode() { return this.ErrorCode; }
}