package com.bloodbond.assistant;

import com.bloodbond.assistant.dto.AssistantExchangeResponse;
import com.bloodbond.assistant.dto.AssistantMessageResponse;
import com.bloodbond.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AssistantService {
    private final AssistantConversationRepository conversationRepository;
    private final AssistantMessageRepository messageRepository;
    private final GroqChatClient groqChatClient;

    public AssistantService(AssistantConversationRepository conversationRepository,
                            AssistantMessageRepository messageRepository,
                            GroqChatClient groqChatClient) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.groqChatClient = groqChatClient;
    }

    @Transactional(readOnly = true)
    public List<AssistantMessageResponse> history(User user) {
        return conversationRepository.findByUserId(user.getId())
            .map(conversation -> messageRepository.findTop100ByConversationIdOrderByIdDesc(conversation.getId())
                .stream().sorted(Comparator.comparing(AssistantMessage::getId))
                .map(this::toResponse).toList())
            .orElseGet(List::of);
    }

    @Transactional
    public AssistantExchangeResponse send(User user, String content) {
        AssistantConversation conversation = conversationRepository.findByUserId(user.getId())
            .orElseGet(() -> {
                AssistantConversation created = new AssistantConversation();
                created.setUser(user);
                return conversationRepository.save(created);
            });

        AssistantMessage userMessage = new AssistantMessage();
        userMessage.setConversation(conversation);
        userMessage.setRole("user");
        userMessage.setContent(content.trim());
        userMessage = messageRepository.save(userMessage);

        List<AssistantMessage> context = messageRepository
            .findTop12ByConversationIdOrderByIdDesc(conversation.getId())
            .stream().sorted(Comparator.comparing(AssistantMessage::getId)).toList();
        String reply = groqChatClient.reply(user.getRole().name(), context);

        AssistantMessage assistantMessage = new AssistantMessage();
        assistantMessage.setConversation(conversation);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(reply);
        AssistantMessage savedReply = messageRepository.save(assistantMessage);
        return new AssistantExchangeResponse(toResponse(userMessage), toResponse(savedReply));
    }

    @Transactional
    public void clear(User user) {
        conversationRepository.findByUserId(user.getId()).ifPresent(conversation -> {
            messageRepository.deleteByConversationId(conversation.getId());
            conversationRepository.delete(conversation);
        });
    }

    private AssistantMessageResponse toResponse(AssistantMessage message) {
        return new AssistantMessageResponse(message.getId(), message.getRole(), message.getContent(), message.getCreatedAt());
    }
}
