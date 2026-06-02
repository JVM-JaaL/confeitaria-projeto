package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gallery_items")
public class GalleryItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(length = 1000)
    private String description;
    private String imagePath;
    private boolean visible = true;
    private int displayOrder = 0;
    private LocalDateTime createdAt = LocalDateTime.now();
}
