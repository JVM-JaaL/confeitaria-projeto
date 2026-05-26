package com.confeitaria.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Data
@Entity
@Table(name = "utm_links")
public class UtmLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    /** URL base completa (ex.: http://localhost:8080/contato) */
    @Column(nullable = false, length = 2000)
    private String baseUrl;

    @Column(nullable = false, length = 200)
    private String utmSource;

    @Column(nullable = false, length = 200)
    private String utmMedium;

    @Column(nullable = false, length = 200)
    private String utmCampaign;

    @Column(length = 500)
    private String utmTerm;

    @Column(length = 500)
    private String utmContent;

    @Column(length = 1000)
    private String shortDescription;

    private Instant createdAt = Instant.now();
    private Instant lastUpdated;
}
