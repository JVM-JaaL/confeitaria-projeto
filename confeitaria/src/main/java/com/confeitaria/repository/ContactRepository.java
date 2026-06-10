package com.confeitaria.repository;

import com.confeitaria.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

// Acesso ao banco para Contact. Além do CRUD básico do JpaRepository, tem queries para o dashboard.
// Usado por: AdminController (dashboard, listagem, exclusão), PublicController (salva contato)
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Lista todos os contatos do mais recente para o mais antigo
    List<Contact> findAllByOrderByCreatedAtDesc();

    // Agrupa contatos por canal de origem — usado no gráfico de fontes do dashboard
    @Query("SELECT c.source, COUNT(c) FROM Contact c GROUP BY c.source")
    List<Object[]> countBySource();

    // Agrupa contatos por código de indicação — usado no relatório de referrals do dashboard
    @Query("SELECT c.referralCode, COUNT(c) FROM Contact c WHERE c.referralCode IS NOT NULL GROUP BY c.referralCode")
    List<Object[]> countByReferralCode();
}
