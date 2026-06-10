package com.confeitaria.repository;

import com.confeitaria.model.GalleryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Acesso ao banco para GalleryItem.
// Usado por: ContentAdminController (CRUD admin), PublicController (lista para o site)
public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {

    // Retorna apenas os itens visíveis, na ordem definida pelo admin — usada no site público
    List<GalleryItem> findByVisibleTrueOrderByDisplayOrderAsc();
}
