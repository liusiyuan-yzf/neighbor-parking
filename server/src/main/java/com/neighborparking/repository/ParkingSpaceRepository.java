package com.neighborparking.repository;

import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.enums.SpaceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, Long> {
    List<ParkingSpace> findAllByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    List<ParkingSpace> findAllByStatusOrderByCreatedAtAsc(SpaceStatus status);
    List<ParkingSpace> findAllByCommunityIdAndStatus(Long communityId, SpaceStatus status);
    Optional<ParkingSpace> findByIdAndOwnerId(Long id, Long ownerId);
}
