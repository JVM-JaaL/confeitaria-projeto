package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

// Representa uma mensagem de contato enviada pelo formulário público (/contato).
// Armazena de onde veio o visitante (UTM + referral) para análise de marketing.
// Usado por: PublicController (cria), AdminController (lista/apaga), ContactRepository (persiste)
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

    // Código de indicação (ex: "INSTAGRAM") rastreado pelo ReferralLink
    private String referralCode;

    // Canal de origem: "instagram", "referral", "google", "site" etc.
    private String source;

    // Parâmetros UTM capturados da URL quando o visitante acessou o site
    private String utmSource;
    private String utmMedium;
    private String utmCampaign;
    private String utmTerm;
    private String utmContent;

    private LocalDateTime createdAt = LocalDateTime.now();
}
