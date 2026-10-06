package com.example.booking.repository;

import com.example.booking.domain.ServiceOffering;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceOfferingRepository extends JpaRepository<ServiceOffering, Long> {
}
