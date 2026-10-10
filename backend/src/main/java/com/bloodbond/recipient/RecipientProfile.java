package com.bloodbond.recipient;

import com.bloodbond.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipient_profiles")
public class RecipientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "city")
    private String city;

    @Column(name = "approx_lat")
    private Double approxLat;

    @Column(name = "approx_lng")
    private Double approxLng;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void setUpdatedAt() {
        this.updatedAt = LocalDateTime.now();
    }

    public RecipientProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public Double getApproxLat() { return approxLat; }
    public void setApproxLat(Double approxLat) { this.approxLat = approxLat; }

    public Double getApproxLng() { return approxLng; }
    public void setApproxLng(Double approxLng) { this.approxLng = approxLng; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
