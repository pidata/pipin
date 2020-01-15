package io.pipin.core.exception;

/**
 * Created by touty on 2020/1/3.
 */
public class JobException extends Exception {
    public JobException(String message) {
        super(message);
    }

    public JobException(String message, Throwable cause) {
        super(message, cause);
    }
}
