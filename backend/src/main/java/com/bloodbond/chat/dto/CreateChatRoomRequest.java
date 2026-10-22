package com.bloodbond.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateChatRoomRequest(
    @NotNull Long donorProfileId,
    @NotBlank @Pattern(regexp = "A\\+|A-|B\\+|B-|AB\\+|AB-|O\\+|O-") String bloodGroup,
    String city,
    Double approxLat,
    Double approxLng
) {}
