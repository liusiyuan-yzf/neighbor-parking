package com.neighborparking.repository;

import com.neighborparking.domain.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findAllByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Vehicle> findByIdAndUserId(Long id, Long userId);
}
