package com.huitshop.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final String SRC_DIR = "C:\\LUUDULIEU\\CODE\\ecommerce-huit-java\\HuitShopDB\\src\\main\\resources\\com\\huitshop\\Anh";
    private final String TARGET_DIR = "C:\\LUUDULIEU\\CODE\\ecommerce-huit-java\\HuitShopDB\\target\\classes\\com\\huitshop\\Anh";

    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "File is empty"));
        }
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "image_" + System.currentTimeMillis() + ".png";
            }
            
            String cleanFileName = originalFilename.replaceAll("[^a-zA-Z0-9.\\-_ ]", "_");
            
            File srcDir = new File(SRC_DIR);
            if (!srcDir.exists()) srcDir.mkdirs();
            Path srcPath = Paths.get(SRC_DIR, cleanFileName);
            file.transferTo(srcPath.toFile());

            File targetDir = new File(TARGET_DIR);
            if (!targetDir.exists()) targetDir.mkdirs();
            Path targetPath = Paths.get(TARGET_DIR, cleanFileName);
            Files.copy(srcPath, targetPath, StandardCopyOption.REPLACE_EXISTING);

            Map<String, String> response = new HashMap<>();
            response.put("filename", cleanFileName);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Could not upload the file: " + e.getMessage()));
        }
    }
}
