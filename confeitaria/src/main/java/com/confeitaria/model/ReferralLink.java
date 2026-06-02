package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "referral_links")
public class ReferralLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code; // unique referral code
    private String referrerName; // who referred
    private int visits = 0;
    private int conversions = 0; // contacts submitted via this link
    private LocalDateTime createdAt = LocalDateTime.now();
}
