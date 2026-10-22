package com.bloodbond.admin;

import com.bloodbond.chat.ChatRoomRepository;
import com.bloodbond.common.ApiResponse;
import com.bloodbond.donor.DonorProfileRepository;
import com.bloodbond.request.BloodRequestRepository;
import com.bloodbond.user.Role;
import com.bloodbond.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final UserRepository userRepository;
    private final DonorProfileRepository donorProfileRepository;
    private final BloodRequestRepository bloodRequestRepository;
    private final ChatRoomRepository chatRoomRepository;

    public AdminController(UserRepository userRepository,
                           DonorProfileRepository donorProfileRepository,
                           BloodRequestRepository bloodRequestRepository,
                           ChatRoomRepository chatRoomRepository) {
        this.userRepository = userRepository;
        this.donorProfileRepository = donorProfileRepository;
        this.bloodRequestRepository = bloodRequestRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewResponse>> overview() {
        AdminOverviewResponse overview = new AdminOverviewResponse(
            userRepository.count(),
            userRepository.countByRole(Role.DONOR),
            userRepository.countByRole(Role.RECIPIENT),
            bloodRequestRepository.countByStatus("OPEN"),
            bloodRequestRepository.countByStatus("FULFILLED"),
            chatRoomRepository.count(),
            donorProfileRepository.countByVerificationStatus("PENDING")
        );
        return ResponseEntity.ok(ApiResponse.<AdminOverviewResponse>builder()
            .status(HttpStatus.OK.value()).message("Admin overview loaded").data(overview).build());
    }
}
