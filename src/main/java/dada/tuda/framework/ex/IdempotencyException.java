package dada.tuda.framework.ex;

public class IdempotencyException extends RuntimeException {
    public IdempotencyException() {
    }

    public IdempotencyException(String message) {
        super(message);
    }

    public IdempotencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public IdempotencyException(Throwable cause) {
        super(cause);
    }

    public IdempotencyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
