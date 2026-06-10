package com.confeitaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;

// Registra os diretórios de imagens como recursos estáticos servidos pelo Spring MVC.
// Sem isso, o Spring não saberia onde buscar os arquivos em /uploads/** e /imagens/**.
// Usado por: ImageUploadService (salva em uploadDir), templates HTML (referenciam /uploads/ e /imagens/)
@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Diretório onde o ImageUploadService salva as fotos enviadas pelo admin (logo, galeria)
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    // Diretório com as imagens originais do negócio (fora do projeto, caminho configurável)
    @Value("${app.images.dir:../Imagens}")
    private String imagesDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** → pasta ./uploads no disco (fotos enviadas pelo painel)
        String uploadsPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath);

        // /imagens/** → pasta ../Imagens (fotos originais da confeiteira)
        String imagesPath = Paths.get(imagesDir).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/imagens/**")
                .addResourceLocations(imagesPath);
    }
}
