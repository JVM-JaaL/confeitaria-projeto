package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "faqs")
public class Faq {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String question;
    @Column(length = 2000)
    private String answer;
    private boolean visible = true;
    private int displayOrder = 0;
}
