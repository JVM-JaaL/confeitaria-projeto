package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contacts")
public class Contact {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String phone;
    @Column(length = 2000)
    private String message;
    private String referralCode;
    private String source; // how they found: instagram, referral, google, etc
    private LocalDateTime createdAt = LocalDateTime.now();
}
