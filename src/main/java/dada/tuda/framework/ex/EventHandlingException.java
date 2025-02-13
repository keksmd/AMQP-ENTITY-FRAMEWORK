package dada.tuda.framework.ex;

public class EventHandlingException extends Exception {
    public EventHandlingException() {
    }

    public EventHandlingException(String message) {
        super(message);
    }

    public EventHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventHandlingException(Throwable cause) {
        super(cause);
    }

    public EventHandlingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
