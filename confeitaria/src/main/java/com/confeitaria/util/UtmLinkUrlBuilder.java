package com.confeitaria.util;

import com.confeitaria.model.UtmLink;
import org.springframework.web.util.UriComponentsBuilder;

public final class UtmLinkUrlBuilder {

    private UtmLinkUrlBuilder() {
    }

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
