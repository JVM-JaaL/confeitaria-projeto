package com.confeitaria.repository;

import com.confeitaria.model.UtmLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Acesso ao banco para UtmLink.
// Usado por: UtmLinkAdminController (CRUD e visualização), DataInitializer (cria o link de exemplo)
public interface UtmLinkRepository extends JpaRepository<UtmLink, Long> {

    // Lista todos os links do mais recente ao mais antigo — exibição na tabela do painel
    List<UtmLink> findAllByOrderByCreatedAtDesc();
}
