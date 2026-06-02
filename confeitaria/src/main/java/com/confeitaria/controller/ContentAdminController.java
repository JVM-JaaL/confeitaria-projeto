package com.confeitaria.controller;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import com.confeitaria.service.ImageUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Controller
@RequestMapping("/admin")
public class ContentAdminController {

    private final GalleryItemRepository galleryRepo;
    private final TestimonialRepository testimonialRepo;
    private final FaqRepository faqRepo;
    private final ImageUploadService imageUploadService;

    public ContentAdminController(GalleryItemRepository galleryRepo,
                                  TestimonialRepository testimonialRepo,
                                  FaqRepository faqRepo,
                                  ImageUploadService imageUploadService) {
        this.galleryRepo = galleryRepo;
        this.testimonialRepo = testimonialRepo;
        this.faqRepo = faqRepo;
        this.imageUploadService = imageUploadService;
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
            String path = imageUploadService.save(file);
            item.setImagePath(path);
            log.info("Imagem adicionada à galeria: {}", path);
        } else {
            log.warn("Upload vazio ignorado para item de galeria");
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
}
