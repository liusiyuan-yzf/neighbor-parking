package com.neighborparking.service;

import com.neighborparking.common.error.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainSupportTest {

    @Test
    void shouldValidateTimeRangeBoundaries() {
        Instant future = Instant.now().plusSeconds(3600);
        assertDoesNotThrow(() -> DomainSupport.requireFutureTimeRange(future, future.plusSeconds(3600)));
        assertThrows(BusinessException.class,
                () -> DomainSupport.requireValidTimeRange(future, future));
        assertThrows(BusinessException.class,
                () -> DomainSupport.requireFutureTimeRange(Instant.now().minusSeconds(60), future));
    }
}
