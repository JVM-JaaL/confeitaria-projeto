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

// Gerencia o conteúdo editorial do site: galeria de fotos, depoimentos e FAQs.
// Separado do AdminController para manter cada controller com responsabilidade única.
// Upload de imagem é delegado ao ImageUploadService — o path retornado é salvo no GalleryItem.
// Todas as rotas exigem autenticação ADMIN (configurado em SecurityConfig).
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

    // GET /admin/galeria — lista todos os itens (incluindo ocultos) para edição
    @GetMapping("/galeria")
    public String galeria(Model model) {
        model.addAttribute("items", galleryRepo.findAll());
        model.addAttribute("newItem", new GalleryItem());
        return "admin/galeria";
    }

    // POST /admin/galeria/add — salva imagem via ImageUploadService e cria o GalleryItem no banco
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

    // POST /admin/galeria/delete/{id} — remove o item (a imagem física no disco não é apagada)
    @PostMapping("/galeria/delete/{id}")
    public String deleteGallery(@PathVariable Long id) {
        galleryRepo.deleteById(id);
        return "redirect:/admin/galeria";
    }

    // POST /admin/galeria/toggle/{id} — alterna visibilidade sem excluir (visible true↔false)
    @PostMapping("/galeria/toggle/{id}")
    public String toggleGallery(@PathVariable Long id) {
        galleryRepo.findById(id).ifPresent(item -> {
            item.setVisible(!item.isVisible());
            galleryRepo.save(item);
        });
        return "redirect:/admin/galeria";
    }

    // ---- TESTIMONIALS ----

    // GET /admin/depoimentos — lista todos os depoimentos para edição
    @GetMapping("/depoimentos")
    public String depoimentos(Model model) {
        model.addAttribute("testimonials", testimonialRepo.findAll());
        model.addAttribute("newTest", new Testimonial());
        return "admin/depoimentos";
    }

    // POST /admin/depoimentos/add — salva novo depoimento
    @PostMapping("/depoimentos/add")
    public String addTestimonial(@ModelAttribute Testimonial t) {
        testimonialRepo.save(t);
        return "redirect:/admin/depoimentos";
    }

    // POST /admin/depoimentos/delete/{id} — remove o depoimento
    @PostMapping("/depoimentos/delete/{id}")
    public String deleteTestimonial(@PathVariable Long id) {
        testimonialRepo.deleteById(id);
        return "redirect:/admin/depoimentos";
    }

    // POST /admin/depoimentos/toggle/{id} — alterna visibilidade no site público
    @PostMapping("/depoimentos/toggle/{id}")
    public String toggleTestimonial(@PathVariable Long id) {
        testimonialRepo.findById(id).ifPresent(t -> {
            t.setVisible(!t.isVisible());
            testimonialRepo.save(t);
        });
        return "redirect:/admin/depoimentos";
    }

    // ---- FAQs ----

    // GET /admin/faqs — lista todas as perguntas frequentes para edição
    @GetMapping("/faqs")
    public String faqs(Model model) {
        model.addAttribute("faqs", faqRepo.findAll());
        model.addAttribute("newFaq", new Faq());
        return "admin/faqs";
    }

    // POST /admin/faqs/add — salva nova FAQ
    @PostMapping("/faqs/add")
    public String addFaq(@ModelAttribute Faq faq) {
        faqRepo.save(faq);
        return "redirect:/admin/faqs";
    }

    // POST /admin/faqs/delete/{id} — remove a FAQ
    @PostMapping("/faqs/delete/{id}")
    public String deleteFaq(@PathVariable Long id) {
        faqRepo.deleteById(id);
        return "redirect:/admin/faqs";
    }
}
