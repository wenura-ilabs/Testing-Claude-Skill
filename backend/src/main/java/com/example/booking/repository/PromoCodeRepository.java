package com.example.booking.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import com.example.booking.domain.PromoCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PromoCodeRepository extends JpaRepository<PromoCode, String> {

	// Row lock (SELECT ... FOR UPDATE), held until the surrounding transaction ends.
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from PromoCode p where p.code = :code")
	Optional<PromoCode> findByCodeForUpdate(@Param("code") String code);

}
