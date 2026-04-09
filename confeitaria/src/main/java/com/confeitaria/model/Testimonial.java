package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "testimonials")
public class Testimonial {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String clientName;
    @Column(length = 1000)
    private String text;
    private int rating; // 1-5
    private boolean visible = true;
    private LocalDateTime createdAt = LocalDateTime.now();
}
