package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.CheckType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "check_record")
public class CheckRecord extends BaseEntity {

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_type", nullable = false, length = 24)
    private CheckType checkType;

    @Column(name = "operator_id", nullable = false)
    private Long operatorId;

    @Column(name = "checked_at", nullable = false)
    private Instant checkedAt;
}
