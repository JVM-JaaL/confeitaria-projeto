package com.confeitaria.web;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Snapshot de ref (indicação) + parâmetros UTM na sessão, para construir links consistentes nas páginas públicas.
 */
@Getter
public class PublicMarketingContext {

    private final String ref;
    private final String utmSource;
    private final String utmMedium;
    private final String utmCampaign;
    private final String utmTerm;
    private final String utmContent;

    public PublicMarketingContext(String ref, String utmSource, String utmMedium,
                                  String utmCampaign, String utmTerm, String utmContent) {
        this.ref = ref;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
        this.utmTerm = utmTerm;
        this.utmContent = utmContent;
    }

    public static PublicMarketingContext fromSession(HttpSession session) {
        return new PublicMarketingContext(
                str(session, PublicMarketingService.SK_REF),
                str(session, PublicMarketingService.SK_UTM_SOURCE),
                str(session, PublicMarketingService.SK_UTM_MEDIUM),
                str(session, PublicMarketingService.SK_UTM_CAMPAIGN),
                str(session, PublicMarketingService.SK_UTM_TERM),
                str(session, PublicMarketingService.SK_UTM_CONTENT)
        );
    }

    private static String str(HttpSession session, String key) {
        Object v = session.getAttribute(key);
        if (v instanceof String s && !s.isBlank()) {
            return s;
        }
        return null;
    }

    /** Caminho absoluto a partir da raiz da aplicação, com query de marketing. */
    public String url(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        UriComponentsBuilder b = UriComponentsBuilder.fromPath(p);
        appendQueryParams(b);
        return b.build().encode().toUriString();
    }

    public void appendQueryParams(UriComponentsBuilder b) {
        if (ref != null && !ref.isBlank()) {
            b.queryParam("ref", ref);
        }
        if (utmSource != null && !utmSource.isBlank()) {
            b.queryParam("utm_source", utmSource);
        }
        if (utmMedium != null && !utmMedium.isBlank()) {
            b.queryParam("utm_medium", utmMedium);
        }
        if (utmCampaign != null && !utmCampaign.isBlank()) {
            b.queryParam("utm_campaign", utmCampaign);
        }
        if (utmTerm != null && !utmTerm.isBlank()) {
            b.queryParam("utm_term", utmTerm);
        }
        if (utmContent != null && !utmContent.isBlank()) {
            b.queryParam("utm_content", utmContent);
        }
    }

    public boolean hasAnyUtm() {
        return utmSource != null || utmMedium != null || utmCampaign != null || utmTerm != null || utmContent != null;
    }
}
