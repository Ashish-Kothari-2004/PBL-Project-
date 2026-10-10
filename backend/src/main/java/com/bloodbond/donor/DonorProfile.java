package com.bloodbond.donor;

import com.bloodbond.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "donor_profiles")
public class DonorProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "city")
    private String city;

    @Column(name = "approx_lat")
    private Double approxLat;

    @Column(name = "approx_lng")
    private Double approxLng;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "verification_status", nullable = false)
    private String verificationStatus = "PENDING";

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "next_eligible_date")
    private LocalDate nextEligibleDate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public DonorProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getApproxLat() { return approxLat; }
    public void setApproxLat(Double approxLat) { this.approxLat = approxLat; }

    public Double getApproxLng() { return approxLng; }
    public void setApproxLng(Double approxLng) { this.approxLng = approxLng; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public LocalDate getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(LocalDate lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public LocalDate getNextEligibleDate() { return nextEligibleDate; }
    public void setNextEligibleDate(LocalDate nextEligibleDate) { this.nextEligibleDate = nextEligibleDate; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
