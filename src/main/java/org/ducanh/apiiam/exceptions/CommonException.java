package org.ducanh.apiiam.exceptions;

import java.text.MessageFormat;

public class CommonException extends RuntimeException {

    public final ErrorCode errorCode;
    public final String longDescription;

    public CommonException(ErrorCode errorCode, String longDescription) {
        this.errorCode = errorCode;
        this.longDescription = longDescription;
    }

    public CommonException(ErrorCode errorCode,
                           String longDescriptionPattern,
                           Object... args) {
        this.errorCode = errorCode;
        this.longDescription = MessageFormat.format(longDescriptionPattern, args);
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("Code: {0}, shortDescriptions: {1}, longDescription: {2}",
                errorCode.code(), errorCode.shortDescriptions, longDescription);
    }

    public CommonException setCause(Throwable ex) {
        this.initCause(ex);
        return this;
    }

}
