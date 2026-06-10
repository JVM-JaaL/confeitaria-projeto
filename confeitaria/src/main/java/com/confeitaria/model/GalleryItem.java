package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Item da galeria de fotos exibido em /galeria.
// imagePath aponta para /uploads/... (foto enviada) ou /imagens/... (foto original do disco).
// visible=false oculta a foto do site sem excluir.
// displayOrder controla a ordem de exibição.
// Gerenciado em: /admin/galeria (ContentAdminController + ImageUploadService)
// Exibido em: PublicController.galeria() → public/galeria.html
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
