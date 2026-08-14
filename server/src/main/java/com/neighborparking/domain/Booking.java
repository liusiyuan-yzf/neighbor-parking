package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "booking", uniqueConstraints = @UniqueConstraint(name = "uk_booking_no", columnNames = "booking_no"))
public class Booking extends BaseEntity {

    @Column(name = "booking_no", nullable = false, length = 48)
    private String bookingNo;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "space_id", nullable = false)
    private Long spaceId;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "renter_id", nullable = false)
    private Long renterId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
}
