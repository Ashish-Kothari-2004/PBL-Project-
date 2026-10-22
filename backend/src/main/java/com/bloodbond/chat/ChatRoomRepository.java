package com.bloodbond.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByDonorIdOrRecipientIdOrderByCreatedAtDesc(Long donorId, Long recipientId);
    Optional<ChatRoom> findByBloodRequestIdAndDonorId(Long requestId, Long donorId);
    Optional<ChatRoom> findFirstByDonorIdAndRecipientIdOrderByCreatedAtDesc(Long donorId, Long recipientId);
}