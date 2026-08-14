package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.SlotStatus;
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
@Table(name = "availability_slot")
public class AvailabilitySlot extends BaseEntity {

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SlotStatus status = SlotStatus.PUBLISHED;

    @Column(name = "free_of_charge", nullable = false)
    private Boolean freeOfCharge = Boolean.TRUE;
}
