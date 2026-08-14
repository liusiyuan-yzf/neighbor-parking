package com.neighborparking.repository;

import com.neighborparking.domain.Booking;
import com.neighborparking.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select count(b) from Booking b where b.spaceId = :spaceId "
            + "and b.status in :statuses and b.startAt < :endAt and b.endAt > :startAt")
    long countOverlaps(@Param("spaceId") Long spaceId,
                       @Param("statuses") Collection<BookingStatus> statuses,
                       @Param("startAt") Instant startAt,
                       @Param("endAt") Instant endAt);

    List<Booking> findAllByRenterIdOrderByCreatedAtDesc(Long renterId);
    List<Booking> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<Booking> findAllByOrderByCreatedAtDesc();
    boolean existsBySlotIdAndStatusIn(Long slotId, Collection<BookingStatus> statuses);
}
