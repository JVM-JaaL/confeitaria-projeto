package com.confeitaria.repository;

import com.confeitaria.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Acesso ao banco para Testimonial.
// Usado por: ContentAdminController (CRUD), PublicController (lista para o site)
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {

    // Retorna apenas os depoimentos visíveis, do mais recente ao mais antigo — exibido no site público
    List<Testimonial> findByVisibleTrueOrderByCreatedAtDesc();
}
