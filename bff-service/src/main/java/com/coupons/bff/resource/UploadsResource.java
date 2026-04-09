package com.coupons.bff.resource;

import com.coupons.bff.domain.service.LocalImageStorageService;
import com.coupons.bff.infra.resource.dto.UploadedImageResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadsResource {

    private final LocalImageStorageService localImageStorageService;

    public UploadsResource(LocalImageStorageService localImageStorageService) {
        this.localImageStorageService = localImageStorageService;
    }

    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UploadedImageResponse uploadImage(@RequestPart("file") MultipartFile file) {
        return localImageStorageService.store(file);
    }

    @GetMapping("/images/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        Resource r = localImageStorageService.load(fileName);
        if (r == null || !r.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(r);
    }
}
