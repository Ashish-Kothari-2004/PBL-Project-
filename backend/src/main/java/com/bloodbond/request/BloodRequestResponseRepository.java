package com.bloodbond.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BloodRequestResponseRepository extends JpaRepository<BloodRequestResponseEntity, Long> {
    List<BloodRequestResponseEntity> findByBloodRequestIdOrderByCreatedAtAsc(Long bloodRequestId);
    Optional<BloodRequestResponseEntity> findByBloodRequestIdAndDonorId(Long bloodRequestId, Long donorId);
    long countByBloodRequestId(Long bloodRequestId);
}
