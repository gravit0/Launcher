package launchserver.auth.hwid;

public class HWIDException extends Exception {
    public HWIDException() {
    }

    public HWIDException(String s) {
        super(s);
    }

    public HWIDException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public HWIDException(Throwable throwable) {
        super(throwable);
    }
}
