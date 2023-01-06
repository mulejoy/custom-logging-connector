package org.liem.extension.logging.exception;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

import java.util.Optional;

public enum LogErrorType implements ErrorTypeDefinition<LogErrorType> {
    INVALID_ARGUMENT;

    private ErrorTypeDefinition<? extends Enum<?>> parent;

    private LogErrorType() {
    }

    private LogErrorType(ErrorTypeDefinition<? extends Enum<?>> parent) {
        this.parent = parent;
    }

    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.ofNullable(this.parent);
    }
}
