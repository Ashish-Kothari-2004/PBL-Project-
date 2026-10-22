package com.bloodbond.assistant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssistantConversationRepository extends JpaRepository<AssistantConversation, Long> {
    Optional<AssistantConversation> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}
