package com.acs_tr069.test_tr069.radius.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.acs_tr069.test_tr069.radius.entity.ApAccounting;

public interface ApAccountingRepository extends JpaRepository<ApAccounting, String> {
    Optional<ApAccounting> findByCalledStationId(String calledStationId);

    Optional<ApAccounting> findBySerialNum(String serialNum);
}
