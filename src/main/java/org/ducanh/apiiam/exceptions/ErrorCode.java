package org.ducanh.apiiam.exceptions;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public enum ErrorCode {

    UNKNOWN_ERROR("001", ErrorGroup.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error"),
    VALIDATION_ERROR("002", ErrorGroup.UNKNOWN, HttpStatus.BAD_REQUEST, "Validation error"),
    NAMESPACE_NOT_EXISTED("003", ErrorGroup.NAMESPACE, HttpStatus.BAD_REQUEST, "Namespace not existed"),
    USERNAME_NOT_EXISTED("004", ErrorGroup.USER, HttpStatus.BAD_REQUEST, "Username not existed"),
    INVALID_OTP("005", ErrorGroup.OTP, HttpStatus.BAD_REQUEST, "Invalid OTP"),
    USER_STATUS_NOT_VALID("006", ErrorGroup.USER, HttpStatus.UNAUTHORIZED, "User status not valid"),
    INVALID_PASSWORD("007", ErrorGroup.PASSWORD, HttpStatus.UNAUTHORIZED, "Invalid password"),
    INVALID_TOKEN("008", ErrorGroup.TOKEN, HttpStatus.UNAUTHORIZED, "Invalid token");
    private final String code;
    private final ErrorGroup errorGroup;
    private final HttpStatus httpStatus;
    public final String shortDescriptions;

    ErrorCode(String code, ErrorGroup errorGroup, HttpStatus httpStatus, String shortDescriptions) {
        this.code = code;
        this.errorGroup = errorGroup;
        this.httpStatus = httpStatus;
        this.shortDescriptions = shortDescriptions;
    }

    public String code() {
        return MessageFormat.format("{0}_{1}_{2}", errorGroup.name(), code, httpStatus.value());
    }

    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
