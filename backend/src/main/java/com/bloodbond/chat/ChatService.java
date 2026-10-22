package com.bloodbond.chat;

import com.bloodbond.chat.dto.ChatMessageResponse;
import com.bloodbond.chat.dto.ChatRoomResponse;
import com.bloodbond.chat.dto.CreateChatRoomRequest;
import com.bloodbond.chat.dto.SendChatMessageRequest;
import com.bloodbond.common.exception.BadRequestException;
import com.bloodbond.common.exception.ResourceNotFoundException;
import com.bloodbond.donor.DonorProfile;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.request.BloodRequest;
import com.bloodbond.request.BloodRequestRepository;
import com.bloodbond.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChatService {
    public record AcceptedRequestRoom(ChatRoomResponse donorRoom, ChatRoomResponse recipientRoom, String recipientUsername) {}

    public record ChatRoomCreation(
        ChatRoomResponse room,
        ChatRoomResponse donorRoom,
        String donorUsername,
        boolean created
    ) {}

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final DonorProfileRepository donorProfileRepository;

    public ChatService(ChatRoomRepository chatRoomRepository, ChatMessageRepository chatMessageRepository,
                       BloodRequestRepository bloodRequestRepository, DonorProfileRepository donorProfileRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.donorProfileRepository = donorProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(User user) {
        return chatRoomRepository.findByDonorIdOrRecipientIdOrderByCreatedAtDesc(user.getId(), user.getId())
            .stream().map(room -> toRoomResponse(room, user)).toList();
    }

    @Transactional
    public ChatRoomCreation createRoom(User recipient, CreateChatRoomRequest request) {
        DonorProfile donorProfile = donorProfileRepository.findById(request.donorProfileId())
            .orElseThrow(() -> new ResourceNotFoundException("Donor not found"));
        if (!donorProfile.isAvailable()) {
            throw new BadRequestException("This donor is not currently available");
        }

        User donor = donorProfile.getUser();
        var existingRoom = chatRoomRepository.findFirstByDonorIdAndRecipientIdOrderByCreatedAtDesc(donor.getId(), recipient.getId());
        if (existingRoom.isPresent()) {
            ChatRoom room = existingRoom.get();
            return new ChatRoomCreation(
                toRoomResponse(room, recipient),
                toRoomResponse(room, donor),
                donor.getUsername(),
                false
            );
        }

        BloodRequest bloodRequest = new BloodRequest();
        bloodRequest.setRecipient(recipient);
        bloodRequest.setBloodGroup(request.bloodGroup());
        bloodRequest.setCity(request.city());
        bloodRequest.setApproxLat(request.approxLat());
        bloodRequest.setApproxLng(request.approxLng());
        bloodRequest.setRadiusKm(50);
        BloodRequest savedRequest = bloodRequestRepository.save(bloodRequest);

        ChatRoom room = new ChatRoom();
        room.setBloodRequest(savedRequest);
        room.setDonor(donor);
        room.setRecipient(recipient);
        ChatRoom savedRoom = chatRoomRepository.save(room);
        return new ChatRoomCreation(
            toRoomResponse(savedRoom, recipient),
            toRoomResponse(savedRoom, donor),
            donor.getUsername(),
            true
        );
    }

    @Transactional
    public AcceptedRequestRoom createRoomForRequest(User donor, BloodRequest request) {
        ChatRoom room = chatRoomRepository.findByBloodRequestIdAndDonorId(request.getId(), donor.getId())
            .orElseGet(() -> {
                ChatRoom created = new ChatRoom();
                created.setBloodRequest(request);
                created.setDonor(donor);
                created.setRecipient(request.getRecipient());
                return chatRoomRepository.save(created);
            });
        return new AcceptedRequestRoom(
            toRoomResponse(room, donor),
            toRoomResponse(room, request.getRecipient()),
            request.getRecipient().getUsername()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(User user, Long roomId) {
        ChatRoom room = getParticipantRoom(user, roomId);
        return chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId()).stream()
            .map(this::toMessageResponse).toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(User user, Long roomId, SendChatMessageRequest request) {
        ChatRoom room = getParticipantRoom(user, roomId);
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSender(user);
        message.setContent(request.content().trim());
        if (message.getContent().isEmpty()) {
            throw new BadRequestException("Message cannot be empty");
        }
        return toMessageResponse(chatMessageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public void verifyParticipant(User user, Long roomId) {
        getParticipantRoom(user, roomId);
    }

    private ChatRoom getParticipantRoom(User user, Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));
        if (!room.getDonor().getId().equals(user.getId()) && !room.getRecipient().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Chat room not found");
        }
        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, User user) {
        User counterpart = room.getDonor().getId().equals(user.getId()) ? room.getRecipient() : room.getDonor();
        return new ChatRoomResponse(
            room.getId(),
            room.getBloodRequest().getId(),
            counterpart.getName(),
            room.getBloodRequest().getBloodGroup(),
            room.getBloodRequest().getCity(),
            room.getCreatedAt()
        );
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return new ChatMessageResponse(
            message.getId(),
            message.getSender().getId(),
            message.getSender().getName(),
            message.getContent(),
            message.getCreatedAt()
        );
    }
}
