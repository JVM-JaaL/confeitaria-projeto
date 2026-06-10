package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Link de indicação (ex.: ?ref=INSTAGRAM). Rastreia visitas e conversões (formulários enviados).
// visits é incrementado em PublicController quando alguém acessa o site com o código.
// conversions é incrementado em PublicController quando o visitante envia o formulário de contato.
// Gerenciado em: /admin (AdminController.addReferral/deleteReferral)
// Exibido no dashboard: AdminController.dashboard()
@Data
@Entity
@Table(name = "referral_links")
public class ReferralLink {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;         // código único, sempre em maiúsculas (ex: "INSTAGRAM")
    private String referrerName; // nome descritivo para o painel (ex: "Instagram Oficial")
    private int visits = 0;
    private int conversions = 0; // contatos enviados via este link
    private LocalDateTime createdAt = LocalDateTime.now();
}
