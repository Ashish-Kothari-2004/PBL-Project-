package com.bloodbond.request;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
    long countByStatus(String status);
    Optional<BloodRequest> findByIdAndRecipientIdAndStatus(Long id, Long recipientId, String status);

    @Query("select request from BloodRequest request where request.status = 'OPEN' "
        + "and not exists (select response.id from BloodRequestResponseEntity response "
        + "where response.bloodRequest = request and response.donor.id = :donorId) "
        + "order by request.createdAt desc")
    List<BloodRequest> findOpenUnansweredByDonor(@Param("donorId") Long donorId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from BloodRequest request where request.id = :id and request.status = 'OPEN'")
    Optional<BloodRequest> findOpenForUpdate(@Param("id") Long id);
}