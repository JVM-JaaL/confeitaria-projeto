package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Depoimento de cliente exibido na página pública /depoimentos.
// visible=false oculta o depoimento sem excluir.
// rating: 1 a 5 estrelas.
// Gerenciado em: /admin/depoimentos (ContentAdminController)
// Exibido em: PublicController.depoimentos() → public/depoimentos.html
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
