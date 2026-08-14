package com.neighborparking.service;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

import java.time.Instant;

public final class DomainSupport {

    private DomainSupport() {
    }

    public static void requireValidTimeRange(Instant startAt, Instant endAt) {
        if (startAt == null || endAt == null || !endAt.isAfter(startAt)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
    }

    public static void requireFutureTimeRange(Instant startAt, Instant endAt) {
        requireValidTimeRange(startAt, endAt);
        if (!startAt.isAfter(Instant.now())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "开始时间必须晚于当前时间");
        }
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, message);
    }
}
