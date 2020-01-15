package io.pipin.core.exception;

import io.pipin.core.domain.Stage;

/**
 * Created by touty on 2020/1/3.
 */
public class StageException extends Exception {
    public StageException(String message) {
        super(message);
    }

    public StageException(String message, Throwable cause) {
        super(message, cause);
    }
}
