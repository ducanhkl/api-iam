package org.ducanh.apiiam.exceptions;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

public enum ErrorCode {

    UNKNOWN_ERROR("001", ErrorGroup.UNKNOWN, HttpStatus.INTERNAL_SERVER_ERROR, "Unknown error"),
    VALIDATION_ERROR("002", ErrorGroup.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Validation error"),
    NAMESPACE_NOT_EXISTED("003", ErrorGroup.NAMESPACE, HttpStatus.BAD_REQUEST, "Namespace not existed"),
    USERNAME_NOT_EXISTED("004", ErrorGroup.USER, HttpStatus.BAD_REQUEST, "Username not existed"),
    INVALID_OTP("005", ErrorGroup.OTP, HttpStatus.BAD_REQUEST, "Invalid OTP"),
    USER_STATUS_NOT_VALID("006", ErrorGroup.USER, HttpStatus.UNAUTHORIZED, "User status not valid"),
    INVALID_PASSWORD("007", ErrorGroup.PASSWORD, HttpStatus.UNAUTHORIZED, "Invalid password"),
    INVALID_TOKEN("008", ErrorGroup.TOKEN, HttpStatus.UNAUTHORIZED, "Invalid token"),
    GROUP_NOT_FOUND("009", ErrorGroup.GROUP, HttpStatus.BAD_REQUEST, "Group not found"),
    GROUP_INFO_DUPLICATED("010", ErrorGroup.GROUP, HttpStatus.BAD_REQUEST, "Group info duplicated"),
    ROLE_NOT_FOUND("011", ErrorGroup.ROLE, HttpStatus.BAD_REQUEST, "Role not found"),
    NAMESPACE_ID_DUPLICATED("012", ErrorGroup.NAMESPACE, HttpStatus.BAD_REQUEST, "NamespaceId duplicated"),
    KEYPAIR_NOT_EXIST("013", ErrorGroup.KEYPAIR, HttpStatus.BAD_REQUEST, "Key pair not exist"),
    PERMISSION_NOT_EXIST("014", ErrorGroup.PERMISSION, HttpStatus.BAD_REQUEST, "Permission not exist"),
    ROLE_ID_DUPLICATED("015", ErrorGroup.ROLE, HttpStatus.BAD_REQUEST, "RoleId is duplicated"),
    USER_ID_NOT_EXISTED("016", ErrorGroup.USER, HttpStatus.BAD_REQUEST, "UserId not exited"),
    TOO_MANY_SESSION("017", ErrorGroup.SESSION, HttpStatus.TOO_MANY_REQUESTS, "Too many session"),
    ;

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
