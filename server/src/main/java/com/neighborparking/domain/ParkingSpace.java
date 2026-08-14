package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.SpaceStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "parking_space")
public class ParkingSpace extends BaseEntity {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "community_id", nullable = false)
    private Long communityId;

    @Column(name = "space_code", nullable = false, length = 64)
    private String spaceCode;

    @Column(nullable = false, length = 128)
    private String title;

    @Column(name = "access_instructions", nullable = false, length = 1000)
    private String accessInstructions;

    @Column(name = "vehicle_limit", nullable = false, length = 64)
    private String vehicleLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SpaceStatus status = SpaceStatus.PENDING_REVIEW;

    @Column(name = "review_note", length = 500)
    private String reviewNote;
}
