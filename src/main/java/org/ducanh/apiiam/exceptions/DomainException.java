package org.ducanh.apiiam.exceptions;

import java.text.MessageFormat;

public class DomainException extends RuntimeException {

    public final ErrorCode errorCode;
    public final String longDescription;

    public DomainException(ErrorCode errorCode, String longDescription) {
        this.errorCode = errorCode;
        this.longDescription = longDescription;
    }

    @Override
    public String getMessage() {
        return MessageFormat.format("Code: {}, shortDescriptions: {}, longDescription: {}",
                errorCode.code(), errorCode.shortDescriptions, longDescription);
    }


}
