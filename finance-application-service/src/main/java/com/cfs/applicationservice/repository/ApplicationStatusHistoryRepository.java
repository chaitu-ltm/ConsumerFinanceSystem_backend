package com.cfs.applicationservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.applicationservice.entity
        .ApplicationStatusHistory;

public interface ApplicationStatusHistoryRepository
        extends JpaRepository<
            ApplicationStatusHistory,
            Long> {

    List<ApplicationStatusHistory>
            findByApplicationIdOrderByChangedAtAsc(
            Long applicationId
    );
}