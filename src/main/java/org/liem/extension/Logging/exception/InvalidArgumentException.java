package org.liem.extension.logging.exception;

import org.mule.runtime.extension.api.exception.ModuleException;

public class InvalidArgumentException extends ModuleException {
    public InvalidArgumentException(Throwable cause) {
        super(LogErrorType.INVALID_ARGUMENT, cause);
    }

    public InvalidArgumentException(String message, Throwable cause) {
        super(message, LogErrorType.INVALID_ARGUMENT, cause);
    }

    public InvalidArgumentException(String message) {
        super(message, LogErrorType.INVALID_ARGUMENT);
    }
}
