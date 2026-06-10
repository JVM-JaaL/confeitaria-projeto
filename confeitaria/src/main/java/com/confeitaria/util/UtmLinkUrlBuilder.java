package com.confeitaria.util;

import com.confeitaria.model.UtmLink;
import org.springframework.web.util.UriComponentsBuilder;

// Utilitário estático que monta a URL completa com parâmetros UTM a partir de um UtmLink.
// Não tem estado — é chamado diretamente sem injeção.
// Usado por: UtmLinkAdminController.visualizar() (exibe a URL gerada na tela de detalhe)
public final class UtmLinkUrlBuilder {

    private UtmLinkUrlBuilder() {
    }

    // Combina baseUrl + utm_source + utm_medium + utm_campaign + (opcionais) utm_term, utm_content
    // Resultado: ex. "https://meusite.com/contato?utm_source=instagram&utm_medium=social&utm_campaign=doces_2026"
    public static String generateFullUrl(UtmLink link) {
        String base = link.getBaseUrl().trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(base);
        b.queryParam("utm_source", link.getUtmSource());
        b.queryParam("utm_medium", link.getUtmMedium());
        b.queryParam("utm_campaign", link.getUtmCampaign());
        if (link.getUtmTerm() != null && !link.getUtmTerm().isBlank()) {
            b.queryParam("utm_term", link.getUtmTerm());
        }
        if (link.getUtmContent() != null && !link.getUtmContent().isBlank()) {
            b.queryParam("utm_content", link.getUtmContent());
        }
        return b.build().encode().toUriString();
    }
}
