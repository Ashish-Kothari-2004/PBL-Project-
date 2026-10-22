package com.bloodbond.admin;

public record AdminOverviewResponse(
    long totalUsers,
    long donors,
    long recipients,
    long openRequests,
    long fulfilledRequests,
    long conversations,
    long pendingDonorReviews
) {}
