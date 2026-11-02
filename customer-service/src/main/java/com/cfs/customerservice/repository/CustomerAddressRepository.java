package com.cfs.customerservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cfs.customerservice.entity.CustomerAddress;

public interface CustomerAddressRepository
        extends JpaRepository<CustomerAddress, Long> {

    List<CustomerAddress> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndAddressType(
            Long customerId,
            String addressType
    );
}