package com.confeitaria.controller;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ContactRepository contactRepo;
    private final GalleryItemRepository galleryRepo;
    private final TestimonialRepository testimonialRepo;
    private final FaqRepository faqRepo;
    private final ReferralLinkRepository referralRepo;
    private final SaleRepository saleRepo;
    private final RecipeRepository recipeRepo;
    private final IngredientRepository ingredientRepo;

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    public AdminController(ContactRepository contactRepo, GalleryItemRepository galleryRepo,
                           TestimonialRepository testimonialRepo, FaqRepository faqRepo,
                           ReferralLinkRepository referralRepo, SaleRepository saleRepo,
                           RecipeRepository recipeRepo, IngredientRepository ingredientRepo) {
        this.contactRepo = contactRepo;
        this.galleryRepo = galleryRepo;
        this.testimonialRepo = testimonialRepo;
        this.faqRepo = faqRepo;
        this.referralRepo = referralRepo;
        this.saleRepo = saleRepo;
        this.recipeRepo = recipeRepo;
        this.ingredientRepo = ingredientRepo;
    }

    @GetMapping("/login")
    public String login() { return "admin/login"; }

    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("totalContacts", contactRepo.count());
        model.addAttribute("totalSales", saleRepo.count());
        model.addAttribute("totalRecipes", recipeRepo.count());
        model.addAttribute("recentContacts", contactRepo.findAllByOrderByCreatedAtDesc().stream().limit(5).toList());
        model.addAttribute("sourceCounts", contactRepo.countBySource());
        model.addAttribute("referralCounts", contactRepo.countByReferralCode());
        model.addAttribute("referralLinks", referralRepo.findAll());
        return "admin/dashboard";
    }

    // ---- CONTACTS ----
    @GetMapping("/contatos")
    public String contatos(Model model) {
        model.addAttribute("contacts", contactRepo.findAllByOrderByCreatedAtDesc());
        model.addAttribute("sourceCounts", contactRepo.countBySource());
        model.addAttribute("referralCounts", contactRepo.countByReferralCode());
        return "admin/contatos";
    }

    @PostMapping("/contatos/delete/{id}")
    public String deleteContato(@PathVariable Long id) {
        contactRepo.deleteById(id);
        return "redirect:/admin/contatos";
    }

    // ---- GALLERY ----
    @GetMapping("/galeria")
    public String galeria(Model model) {
        model.addAttribute("items", galleryRepo.findAll());
        model.addAttribute("newItem", new GalleryItem());
        return "admin/galeria";
    }

    @PostMapping("/galeria/add")
    public String addGallery(@ModelAttribute GalleryItem item,
                             @RequestParam("imageFile") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            item.setImagePath(saveFile(file));
        }
        galleryRepo.save(item);
        return "redirect:/admin/galeria";
    }

    @PostMapping("/galeria/delete/{id}")
    public String deleteGallery(@PathVariable Long id) {
        galleryRepo.deleteById(id);
        return "redirect:/admin/galeria";
    }

    @PostMapping("/galeria/toggle/{id}")
    public String toggleGallery(@PathVariable Long id) {
        galleryRepo.findById(id).ifPresent(item -> {
            item.setVisible(!item.isVisible());
            galleryRepo.save(item);
        });
        return "redirect:/admin/galeria";
    }

    // ---- TESTIMONIALS ----
    @GetMapping("/depoimentos")
    public String depoimentos(Model model) {
        model.addAttribute("testimonials", testimonialRepo.findAll());
        model.addAttribute("newTest", new Testimonial());
        return "admin/depoimentos";
    }

    @PostMapping("/depoimentos/add")
    public String addTestimonial(@ModelAttribute Testimonial t) {
        testimonialRepo.save(t);
        return "redirect:/admin/depoimentos";
    }

    @PostMapping("/depoimentos/delete/{id}")
    public String deleteTestimonial(@PathVariable Long id) {
        testimonialRepo.deleteById(id);
        return "redirect:/admin/depoimentos";
    }

    @PostMapping("/depoimentos/toggle/{id}")
    public String toggleTestimonial(@PathVariable Long id) {
        testimonialRepo.findById(id).ifPresent(t -> {
            t.setVisible(!t.isVisible());
            testimonialRepo.save(t);
        });
        return "redirect:/admin/depoimentos";
    }

    // ---- FAQs ----
    @GetMapping("/faqs")
    public String faqs(Model model) {
        model.addAttribute("faqs", faqRepo.findAll());
        model.addAttribute("newFaq", new Faq());
        return "admin/faqs";
    }

    @PostMapping("/faqs/add")
    public String addFaq(@ModelAttribute Faq faq) {
        faqRepo.save(faq);
        return "redirect:/admin/faqs";
    }

    @PostMapping("/faqs/delete/{id}")
    public String deleteFaq(@PathVariable Long id) {
        faqRepo.deleteById(id);
        return "redirect:/admin/faqs";
    }

    // ---- REFERRAL LINKS ----
    @PostMapping("/referral/add")
    public String addReferral(@RequestParam String code, @RequestParam String referrerName) {
        var ref = new ReferralLink();
        ref.setCode(code.toUpperCase());
        ref.setReferrerName(referrerName);
        referralRepo.save(ref);
        return "redirect:/admin";
    }

    @PostMapping("/referral/delete/{id}")
    public String deleteReferral(@PathVariable Long id) {
        referralRepo.deleteById(id);
        return "redirect:/admin";
    }

    // ---- UTIL ----
    private String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }
}
