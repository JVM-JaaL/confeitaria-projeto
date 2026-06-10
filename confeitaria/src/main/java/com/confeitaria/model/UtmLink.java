package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

// Link de marketing com parâmetros UTM. A URL final é montada por UtmLinkUrlBuilder.generateFullUrl().
// Usado para criar links rastreáveis para redes sociais e campanhas (ex.: Instagram → formulário de contato).
// Gerenciado em: /admin/links-utm (UtmLinkAdminController)
// A URL gerada é copiada e colada nas publicações — não há redirecionamento automático pelo sistema.
@Data
@Entity
@Table(name = "utm_links")
public class UtmLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name; // nome descritivo para o painel (ex: "Instagram → Contato junho")

    /** URL base completa (ex.: http://localhost:8080/contato) — ajustar para o domínio em produção */
    @Column(nullable = false, length = 2000)
    private String baseUrl;

    @Column(nullable = false, length = 200)
    private String utmSource; // ex: "instagram"

    @Column(nullable = false, length = 200)
    private String utmMedium; // ex: "social"

    @Column(nullable = false, length = 200)
    private String utmCampaign; // ex: "doces_2026"

    @Column(length = 500)
    private String utmTerm; // opcional — palavra-chave

    @Column(length = 500)
    private String utmContent; // opcional — variante do anúncio

    @Column(length = 1000)
    private String shortDescription;

    private Instant createdAt = Instant.now();
    private Instant lastUpdated;
}
