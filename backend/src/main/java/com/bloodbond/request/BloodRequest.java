package com.bloodbond.request;

import com.bloodbond.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user_id", nullable = false)
    private User recipient;

    @Column(name = "blood_group", nullable = false)
    private String bloodGroup;

    @Column(name = "city")
    private String city;

    @Column(name = "approx_lat")
    private Double approxLat;

    @Column(name = "approx_lng")
    private Double approxLng;

    @Column(name = "radius_km", nullable = false)
    private int radiusKm;

    @Column(name = "status", nullable = false)
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Double getApproxLat() { return approxLat; }
    public void setApproxLat(Double approxLat) { this.approxLat = approxLat; }
    public Double getApproxLng() { return approxLng; }
    public void setApproxLng(Double approxLng) { this.approxLng = approxLng; }
    public int getRadiusKm() { return radiusKm; }
    public void setRadiusKm(int radiusKm) { this.radiusKm = radiusKm; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}