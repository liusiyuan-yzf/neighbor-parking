package com.neighborparking.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusinessException(BusinessException exception, HttpServletRequest request) {
        ApiError error = new ApiError(exception.getErrorCode().name(), exception.getMessage(),
                request.getRequestURI(), Instant.now(), null);
        return ResponseEntity.status(exception.getStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException exception,
                                                               HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<String, String>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        ApiError error = new ApiError(ErrorCode.VALIDATION_FAILED.name(), "请求参数校验失败",
                request.getRequestURI(), Instant.now(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(AccessDeniedException exception,
                                                                HttpServletRequest request) {
        ApiError error = new ApiError(ErrorCode.FORBIDDEN.name(), "无权执行该操作",
                request.getRequestURI(), Instant.now(), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrityViolationException(DataIntegrityViolationException exception,
                                                                          HttpServletRequest request) {
        LOGGER.warn("数据唯一性或完整性冲突，path={}", request.getRequestURI());
        ApiError error = new ApiError(ErrorCode.DUPLICATE_RESOURCE.name(), "数据已存在或仍被其他记录使用",
                request.getRequestURI(), Instant.now(), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        LOGGER.error("未处理异常，path={}", request.getRequestURI(), exception);
        ApiError error = new ApiError(ErrorCode.INTERNAL_ERROR.name(), "服务暂时不可用",
                request.getRequestURI(), Instant.now(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
