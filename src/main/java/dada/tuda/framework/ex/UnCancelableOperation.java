package dada.tuda.framework.ex;

public class UnCancelableOperation extends Exception {
    public UnCancelableOperation() {
    }

    public UnCancelableOperation(String message) {
        super(message);
    }

    public UnCancelableOperation(String message, Throwable cause) {
        super(message, cause);
    }

    public UnCancelableOperation(Throwable cause) {
        super(cause);
    }

    public UnCancelableOperation(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
