package com.confeitaria.repository;

import com.confeitaria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

// Contact
public interface ContactRepository extends JpaRepository<Contact, Long> {
    List<Contact> findAllByOrderByCreatedAtDesc();
    @Query("SELECT c.source, COUNT(c) FROM Contact c GROUP BY c.source")
    List<Object[]> countBySource();
    @Query("SELECT c.referralCode, COUNT(c) FROM Contact c WHERE c.referralCode IS NOT NULL GROUP BY c.referralCode")
    List<Object[]> countByReferralCode();
}
