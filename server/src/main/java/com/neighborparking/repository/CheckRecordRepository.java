package com.neighborparking.repository;

import com.neighborparking.domain.CheckRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckRecordRepository extends JpaRepository<CheckRecord, Long> {
}
