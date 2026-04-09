package com.confeitaria.repository;
import com.confeitaria.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FaqRepository extends JpaRepository<Faq, Long> {
    List<Faq> findByVisibleTrueOrderByDisplayOrderAsc();
}
