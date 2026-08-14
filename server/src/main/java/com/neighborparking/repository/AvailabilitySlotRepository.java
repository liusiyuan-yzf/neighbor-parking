package com.neighborparking.repository;

import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AvailabilitySlot> findLockedById(Long id);

    List<AvailabilitySlot> findAllBySpaceIdOrderByStartAtDesc(Long spaceId);

    List<AvailabilitySlot> findAllBySpaceIdInAndStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
            List<Long> spaceIds, SlotStatus status, Instant startAt, Instant endAt);

    Optional<AvailabilitySlot> findByIdAndSpaceId(Long id, Long spaceId);
}
