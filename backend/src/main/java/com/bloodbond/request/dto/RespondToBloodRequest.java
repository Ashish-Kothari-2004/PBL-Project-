package com.bloodbond.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RespondToBloodRequest(
    @NotBlank(message = "Choose accept or decline")
    @Pattern(regexp = "ACCEPTED|DECLINED", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Response must be ACCEPTED or DECLINED")
    String decision
) {}
