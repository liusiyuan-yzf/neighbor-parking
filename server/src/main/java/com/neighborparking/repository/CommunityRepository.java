package com.neighborparking.repository;

import com.neighborparking.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityRepository extends JpaRepository<Community, Long> {
    List<Community> findAllByActiveTrueOrderByNameAsc();
}
