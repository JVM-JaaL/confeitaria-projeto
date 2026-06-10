package com.confeitaria.repository;

import com.confeitaria.model.ReferralLink;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Acesso ao banco para ReferralLink.
// Usado por: AdminController (CRUD), PublicController (incrementa visitas e conversões)
public interface ReferralLinkRepository extends JpaRepository<ReferralLink, Long> {

    // Busca por código (ex: "INSTAGRAM") — usado para registrar visita e conversão quando o visitante usa ?ref=
    Optional<ReferralLink> findByCode(String code);
}
