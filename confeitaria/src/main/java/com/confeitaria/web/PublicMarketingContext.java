package com.confeitaria.web;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;

// Snapshot imutável dos parâmetros de marketing (ref + UTM) da sessão do visitante.
// É criado por fromSession() em cada requisição e injetado no model Thymeleaf como "marketing".
// Os templates usam marketing.url("/contato") para gerar links que mantêm o rastreamento enquanto
// o visitante navega entre as páginas do site.
// Usado por: PublicMarketingService (cria e popula), todos os templates públicos (leem)
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

    // Reconstrói o contexto a partir dos atributos salvos na sessão HTTP pelo PublicMarketingService
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

    // Gera um caminho absoluto (ex: "/contato") com os parâmetros de marketing adicionados como query string.
    // Chamado nos templates: th:href="${marketing.url('/contato')}"
    public String url(String path) {
        String p = path.startsWith("/") ? path : "/" + path;
        UriComponentsBuilder b = UriComponentsBuilder.fromPath(p);
        appendQueryParams(b);
        return b.build().encode().toUriString();
    }

    // Adiciona ref e UTM a qualquer UriComponentsBuilder — usado tanto em url() quanto no redirect
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

    // Verifica se há algum parâmetro UTM ativo — usado para decidir se exibe badge de rastreamento
    public boolean hasAnyUtm() {
        return utmSource != null || utmMedium != null || utmCampaign != null || utmTerm != null || utmContent != null;
    }
}
