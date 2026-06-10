package com.confeitaria.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

// Salva imagens enviadas pelo painel admin no disco e retorna o caminho relativo para armazenar no banco.
// O diretório é servido como recurso estático pelo WebConfig (/uploads/**).
// Usado por: ContentAdminController (upload de fotos da galeria e logo)
@Service
@Slf4j
public class ImageUploadService {

    // Diretório físico de destino — configurável via app.upload.dir no application.properties
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // Salva o arquivo com nome único (UUID + nome original) para evitar colisões.
    // Cria o diretório se ainda não existir.
    // Retorna o caminho acessível pela web: "/uploads/uuid_nomeoriginal.ext"
    public String save(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        log.info("Imagem salva: {}", filename);
        return "/uploads/" + filename;
    }
}
