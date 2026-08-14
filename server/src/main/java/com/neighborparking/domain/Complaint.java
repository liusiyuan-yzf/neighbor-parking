package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import com.neighborparking.domain.enums.ComplaintStatus;
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
@Table(name = "complaint")
public class Complaint extends BaseEntity {

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "complainant_id", nullable = false)
    private Long complainantId;

    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ComplaintStatus status = ComplaintStatus.OPEN;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;
}
