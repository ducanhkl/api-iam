package org.ducanh.apiiam.config;

import lombok.extern.slf4j.Slf4j;
import org.ducanh.apiiam.dto.responses.ErrorResponseDto;
import org.ducanh.apiiam.exceptions.CommonException;
import org.ducanh.apiiam.exceptions.ErrorCode;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionsHandle {

    private final BuildProperties buildProperties;
    private final Boolean isLogErrorDetail;

    @Autowired
    public GlobalExceptionsHandle(
            final BuildProperties buildProperties,
            @Value("${app.exceptions-handle.log-error-detail}")
            final Boolean isLogErrorDetail
    ) {
        this.buildProperties = buildProperties;
        this.isLogErrorDetail = isLogErrorDetail;
    }

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ErrorResponseDto> handleDomainException(CommonException ex, WebRequest request) {
        ErrorCode errorCode = ex.errorCode;
        ErrorResponseDto errorResponse =  ErrorResponseDto.builder()
                .errorCode(errorCode.code())
                .shortDescriptions(errorCode.shortDescriptions)
                .longDescription(ex.longDescription)
                .appName(buildProperties.getName())
                .appVersion(buildProperties.getVersion()).build();
        logError(ex);
        return new ResponseEntity<>(errorResponse, errorCode.httpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleUnknownException(Exception ex, WebRequest request) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .errorCode(ErrorCode.UNKNOWN_ERROR.code())
                .shortDescriptions(ErrorCode.UNKNOWN_ERROR.shortDescriptions)
                .longDescription(ex.getMessage())
                .appName(buildProperties.getName())
                .appVersion(buildProperties.getVersion())
                .build();
        logError(ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponseDto> handleValidationException(Exception ex, WebRequest request) {
        ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.code())
                .shortDescriptions(ErrorCode.VALIDATION_ERROR.shortDescriptions)
                .longDescription(ex.getMessage())
                .appName(buildProperties.getName())
                .appVersion(buildProperties.getVersion())
                .build();
        logError(ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private void logError(Exception ex) {
        if (isLogErrorDetail) {
            log.error("Error detail", ex);
            return;
        }
        log.error("Error: {}, message: {}", ex, ex.getMessage());
    }

}
