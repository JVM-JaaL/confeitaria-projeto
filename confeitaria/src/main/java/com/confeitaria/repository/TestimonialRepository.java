package com.confeitaria.repository;

import com.confeitaria.model.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findByVisibleTrueOrderByCreatedAtDesc();
}
