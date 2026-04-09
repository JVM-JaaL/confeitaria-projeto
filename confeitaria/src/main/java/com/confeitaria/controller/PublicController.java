package com.confeitaria.controller;

import com.confeitaria.model.Contact;
import com.confeitaria.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
public class PublicController {

    private final GalleryItemRepository galleryRepo;
    private final TestimonialRepository testimonialRepo;
    private final FaqRepository faqRepo;
    private final ContactRepository contactRepo;
    private final ReferralLinkRepository referralRepo;

    public PublicController(GalleryItemRepository galleryRepo, TestimonialRepository testimonialRepo,
                            FaqRepository faqRepo, ContactRepository contactRepo,
                            ReferralLinkRepository referralRepo) {
        this.galleryRepo = galleryRepo;
        this.testimonialRepo = testimonialRepo;
        this.faqRepo = faqRepo;
        this.contactRepo = contactRepo;
        this.referralRepo = referralRepo;
    }

    @GetMapping("/")
    public String index(@RequestParam(required = false) String ref, Model model, HttpServletRequest request) {
        model.addAttribute("gallery", galleryRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        model.addAttribute("testimonials", testimonialRepo.findByVisibleTrueOrderByCreatedAtDesc());
        model.addAttribute("contact", new Contact());
        model.addAttribute("ref", ref);

        // Track referral visit
        if (ref != null && !ref.isBlank()) {
            referralRepo.findByCode(ref.toUpperCase()).ifPresent(link -> {
                link.setVisits(link.getVisits() + 1);
                referralRepo.save(link);
            });
        }
        return "public/index";
    }

    @GetMapping("/faq")
    public String faq(Model model) {
        model.addAttribute("faqs", faqRepo.findByVisibleTrueOrderByDisplayOrderAsc());
        return "public/faq";
    }

    @GetMapping("/contato")
    public String contato(@RequestParam(required = false) String ref, Model model) {
        model.addAttribute("contact", new Contact());
        model.addAttribute("ref", ref);
        return "public/contato";
    }

    @PostMapping("/contato")
    public String submitContato(@ModelAttribute Contact contact,
                                @RequestParam(required = false) String ref,
                                @RequestParam(required = false) String source) {
        contact.setCreatedAt(LocalDateTime.now());
        contact.setSource(source != null ? source : "site");
        if (ref != null && !ref.isBlank()) {
            contact.setReferralCode(ref.toUpperCase());
            // Count as conversion
            referralRepo.findByCode(ref.toUpperCase()).ifPresent(link -> {
                link.setConversions(link.getConversions() + 1);
                referralRepo.save(link);
            });
        }
        contactRepo.save(contact);
        return "redirect:/contato?sucesso=true";
    }
}
