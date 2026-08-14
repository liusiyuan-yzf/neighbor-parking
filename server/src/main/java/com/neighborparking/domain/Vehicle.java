package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.VerificationStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@Entity
@Table(name = "vehicle", uniqueConstraints = @UniqueConstraint(name = "uk_vehicle_plate", columnNames = "plate_number"))
public class Vehicle extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "plate_number", nullable = false, length = 24)
    private String plateNumber;

    @Column(name = "vehicle_type", nullable = false, length = 32)
    private String vehicleType;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 24)
    private VerificationStatus verificationStatus = VerificationStatus.APPROVED;

    @Column(nullable = false)
    private Boolean active = Boolean.TRUE;
}
