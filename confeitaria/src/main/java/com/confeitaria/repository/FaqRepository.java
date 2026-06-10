package com.confeitaria.repository;

import com.confeitaria.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Acesso ao banco para FAQ.
// Usado por: ContentAdminController (CRUD admin), PublicController (lista para o site)
public interface FaqRepository extends JpaRepository<Faq, Long> {

    // Retorna apenas as FAQs visíveis, ordenadas pelo displayOrder — usada no site público
    List<Faq> findByVisibleTrueOrderByDisplayOrderAsc();
}
