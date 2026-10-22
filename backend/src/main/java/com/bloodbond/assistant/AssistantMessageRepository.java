package com.bloodbond.assistant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssistantMessageRepository extends JpaRepository<AssistantMessage, Long> {
    List<AssistantMessage> findTop12ByConversationIdOrderByIdDesc(Long conversationId);
    List<AssistantMessage> findTop100ByConversationIdOrderByIdDesc(Long conversationId);
    void deleteByConversationId(Long conversationId);
}
