package com.confeitaria.repository;

import com.confeitaria.model.UtmLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtmLinkRepository extends JpaRepository<UtmLink, Long> {

    List<UtmLink> findAllByOrderByCreatedAtDesc();
}
