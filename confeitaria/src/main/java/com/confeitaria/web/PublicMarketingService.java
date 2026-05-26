package com.confeitaria.web;

import com.confeitaria.model.Contact;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class PublicMarketingService {

    public static final String SK_REF = "marketing.ref";
    public static final String SK_UTM_SOURCE = "marketing.utm_source";
    public static final String SK_UTM_MEDIUM = "marketing.utm_medium";
    public static final String SK_UTM_CAMPAIGN = "marketing.utm_campaign";
    public static final String SK_UTM_TERM = "marketing.utm_term";
    public static final String SK_UTM_CONTENT = "marketing.utm_content";

    public void enrichPublicModel(Model model, HttpServletRequest request, HttpSession session, String refParam) {
        syncQueryParamsToSession(request, session);
        if (refParam != null && !refParam.isBlank()) {
            session.setAttribute(SK_REF, refParam.trim().toUpperCase());
        }

        PublicMarketingContext ctx = PublicMarketingContext.fromSession(session);
        model.addAttribute("marketing", ctx);
        model.addAttribute("ref", ctx.getRef());
    }

    private void syncQueryParamsToSession(HttpServletRequest request, HttpSession session) {
        putIfPresent(session, SK_UTM_SOURCE, request.getParameter("utm_source"));
        putIfPresent(session, SK_UTM_MEDIUM, request.getParameter("utm_medium"));
        putIfPresent(session, SK_UTM_CAMPAIGN, request.getParameter("utm_campaign"));
        putIfPresent(session, SK_UTM_TERM, request.getParameter("utm_term"));
        putIfPresent(session, SK_UTM_CONTENT, request.getParameter("utm_content"));
    }

    private void putIfPresent(HttpSession session, String key, String value) {
        if (value != null && !value.isBlank()) {
            session.setAttribute(key, value.trim());
        }
    }

    public void applyStoredMarketingToContact(Contact contact, HttpSession session) {
        contact.setUtmSource(stringAttr(session, SK_UTM_SOURCE));
        contact.setUtmMedium(stringAttr(session, SK_UTM_MEDIUM));
        contact.setUtmCampaign(stringAttr(session, SK_UTM_CAMPAIGN));
        contact.setUtmTerm(stringAttr(session, SK_UTM_TERM));
        contact.setUtmContent(stringAttr(session, SK_UTM_CONTENT));
    }

    private static String stringAttr(HttpSession session, String key) {
        Object v = session.getAttribute(key);
        return v instanceof String s && !s.isBlank() ? s : null;
    }

    /** redirect:/path com parâmetros extras + marketing da sessão (ref + UTM). */
    public String redirectWithMarketing(String path, HttpSession session, Map<String, String> extraQueryParams) {
        String p = path.startsWith("/") ? path : "/" + path;
        UriComponentsBuilder b = UriComponentsBuilder.fromPath(p);
        if (extraQueryParams != null) {
            extraQueryParams.forEach((k, v) -> {
                if (v != null) {
                    b.queryParam(k, v);
                }
            });
        }
        PublicMarketingContext.fromSession(session).appendQueryParams(b);
        return "redirect:" + b.build().encode().toUriString();
    }
}
