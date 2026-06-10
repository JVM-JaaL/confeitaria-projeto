package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

// Pergunta e resposta exibida na página pública /perguntas-frequentes.
// visible=false oculta a FAQ do site sem excluir.
// displayOrder controla a ordem de exibição.
// Gerenciado em: /admin/faqs (ContentAdminController)
// Exibido em: PublicController.faq() → public/faq.html
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
