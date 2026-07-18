package com.coupons.bff.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Copia imagens de seed do classpath ({@code classpath:seed-uploads/*}) para o diretório de
 * uploads do BFF, com nomes fixos usados pelo bootstrap do campaigns-service.
 */
@Component
public class SeedUploadsBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedUploadsBootstrapRunner.class);

    private final Path uploadsRoot;
    private final boolean enabled;

    public SeedUploadsBootstrapRunner(
            @Value("${coupons.uploads.dir:./uploads}") String dir,
            @Value("${coupons.seed-uploads.enabled:true}") boolean enabled) {
        this.uploadsRoot = Paths.get(dir).toAbsolutePath().normalize();
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!enabled) {
            return;
        }
        Files.createDirectories(uploadsRoot);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:seed-uploads/*");
        int copied = 0;
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename == null || filename.isBlank() || filename.startsWith(".")) {
                continue;
            }
            Path target = uploadsRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadsRoot)) {
                continue;
            }
            try (InputStream in = resource.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (IOException ex) {
                log.warn("Falha ao copiar imagem de seed {}: {}", filename, ex.getMessage());
            }
        }
        log.info("Imagens de seed disponíveis em {} ({} ficheiro(s))", uploadsRoot, copied);
    }
}
