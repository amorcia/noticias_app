package com.noticias.api.servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AlmacenamientoServicio {

    private final Path rootLocation = Paths.get("uploads");

    public AlmacenamientoServicio() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento", e);
        }
    }

    public String almacenar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Fallo al almacenar archivo vacío.");
            }
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                    .normalize().toAbsolutePath();

            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + filename; // URL relativa para servir
        } catch (IOException e) {
            throw new RuntimeException("Fallo al almacenar archivo.", e);
        }
    }
}
