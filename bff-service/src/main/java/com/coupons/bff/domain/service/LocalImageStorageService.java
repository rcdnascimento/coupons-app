package com.coupons.bff.domain.service;

import com.coupons.bff.infra.resource.dto.UploadedImageResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageStorageService {

    private static final Set<String> ALLOWED_EXT =
            Set.of("png", "jpg", "jpeg", "webp", "gif", "svg");

    private final Path root;

    public LocalImageStorageService(@Value("${coupons.uploads.dir:./uploads}") String dir) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
    }

    public UploadedImageResponse store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("ficheiro de imagem é obrigatório");
        }
        String ext = extension(file.getOriginalFilename());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("extensão inválida; use png, jpg, jpeg, webp, gif ou svg");
        }
        try {
            Files.createDirectories(root);
            String name = UUID.randomUUID() + "." + ext;
            Path target = root.resolve(name).normalize();
            if (!target.startsWith(root)) {
                throw new IllegalArgumentException("caminho de ficheiro inválido");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new UploadedImageResponse("/api/uploads/images/" + name);
        } catch (IOException ex) {
            throw new IllegalStateException("falha ao guardar imagem em disco", ex);
        }
    }

    public Resource load(String fileName) {
        try {
            String safe = sanitize(fileName);
            Path p = root.resolve(safe).normalize();
            if (!p.startsWith(root)) {
                throw new IllegalArgumentException("nome de ficheiro inválido");
            }
            if (!Files.exists(p)) {
                return null;
            }
            return new UrlResource(p.toUri());
        } catch (Exception ex) {
            throw new IllegalStateException("falha ao ler imagem", ex);
        }
    }

    private String sanitize(String fileName) {
        return fileName.replace("\\", "").replace("/", "").trim();
    }

    private String extension(String original) {
        String s = original == null ? "" : original.trim();
        int idx = s.lastIndexOf('.');
        if (idx < 0 || idx == s.length() - 1) return "";
        return s.substring(idx + 1).toLowerCase(Locale.ROOT);
    }
}
