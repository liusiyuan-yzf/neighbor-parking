package com.neighborparking.domain;

import com.neighborparking.common.domain.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Getter
@Setter
@Entity
@Table(name = "review", uniqueConstraints = @UniqueConstraint(name = "uk_review_booking", columnNames = "booking_id"))
public class Review extends BaseEntity {

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "reviewer_id", nullable = false)
    private Long reviewerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 500)
    private String content;
}
