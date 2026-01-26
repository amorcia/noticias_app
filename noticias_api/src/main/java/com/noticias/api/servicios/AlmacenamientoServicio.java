package com.noticias.api.servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service
public class AlmacenamientoServicio {

    private final Path rootLocation = Paths.get("/uploads");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int MAX_WIDTH = 1920;
    private static final int MAX_HEIGHT = 1080;
    private static final float JPEG_QUALITY = 0.85f;
    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/avif");

    public AlmacenamientoServicio() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo inicializar el almacenamiento", e);
        }
    }

    /**
     * Almacena un archivo convirtiéndolo a Base64 para guardar en BD
     */
    public String almacenar(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("El archivo está vacío");
            }

            System.out.println("=== INICIO ALMACENAMIENTO (BASE64) ===");
            System.out.println("Archivo original: " + file.getOriginalFilename());
            System.out.println("Tamaño original: " + file.getSize() + " bytes");
            System.out.println("Content-Type: " + file.getContentType());

            // Validar tipo de archivo
            validarTipoImagen(file);
            System.out.println("✓ Validación de tipo OK");

            // Validar tamaño
            validarTamañoArchivo(file);
            System.out.println("✓ Validación de tamaño OK");

            // Optimizar imagen
            byte[] imageData = optimizarImagen(file);
            System.out.println("✓ Imagen optimizada. Tamaño final: " + imageData.length + " bytes");

            // Convertir a Base64
            String base64 = java.util.Base64.getEncoder().encodeToString(imageData);
            String mimeType = obtenerMimeType(file);

            // Crear data URL
            String dataUrl = "data:" + mimeType + ";base64," + base64;

            System.out.println("✓ Imagen convertida a Base64");
            System.out.println("MIME Type: " + mimeType);
            System.out.println("Tamaño Base64: " + dataUrl.length() + " caracteres");
            System.out.println("=== FIN ALMACENAMIENTO ===\n");

            return dataUrl;

        } catch (IOException e) {
            System.err.println("❌ Error al procesar imagen: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al procesar imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el MIME type de la imagen
     */
    private String obtenerMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && contentType.startsWith("image/")) {
            return contentType;
        }

        // Fallback basado en extensión
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String ext = obtenerExtension(filename);
            switch (ext.toLowerCase()) {
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
                case "webp":
                    return "image/webp";
                case "avif":
                    return "image/avif";
            }
        }

        return "image/jpeg"; // Default
    }

    /**
     * Valida que el archivo sea una imagen permitida
     */
    public void validarTipoImagen(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de archivo no permitido. Solo se aceptan: JPEG, PNG, WebP, AVIF");
        }

        // Validar extensión también
        String filename = file.getOriginalFilename();
        if (filename == null || !tieneExtensionValida(filename)) {
            throw new IllegalArgumentException("Extensión de archivo no válida");
        }
    }

    /**
     * Valida que el archivo no exceda el tamaño máximo
     */
    public void validarTamañoArchivo(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            double sizeMB = file.getSize() / (1024.0 * 1024.0);
            throw new IllegalArgumentException(
                    String.format("Archivo demasiado grande (%.2f MB). Máximo permitido: 5 MB", sizeMB));
        }
    }

    /**
     * Optimiza la imagen: redimensiona si es necesario y comprime
     */
    public byte[] optimizarImagen(MultipartFile file) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        if (originalImage == null) {
            throw new IllegalArgumentException("No se pudo leer la imagen. Archivo corrupto o formato inválido");
        }

        // Verificar si necesita redimensionamiento
        BufferedImage processedImage = originalImage;
        if (originalImage.getWidth() > MAX_WIDTH || originalImage.getHeight() > MAX_HEIGHT) {
            processedImage = redimensionarImagen(originalImage, MAX_WIDTH, MAX_HEIGHT);
        }

        // Comprimir imagen
        return comprimirImagen(processedImage, obtenerFormatoImagen(file));
    }

    /**
     * Redimensiona la imagen manteniendo el aspect ratio
     */
    private BufferedImage redimensionarImagen(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        // Calcular nuevas dimensiones manteniendo aspect ratio
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        // Redimensionar
        Image scaledImage = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = resizedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();

        return resizedImage;
    }

    /**
     * Comprime la imagen con calidad óptima
     */
    private byte[] comprimirImagen(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if ("jpeg".equalsIgnoreCase(format) || "jpg".equalsIgnoreCase(format)) {
            // Comprimir JPEG con calidad específica
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                throw new IllegalStateException("No JPEG writer found");
            }

            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(JPEG_QUALITY);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(image, null, null), param);
                writer.dispose();
            }
        } else {
            // Para PNG y otros formatos, usar compresión estándar
            ImageIO.write(image, format, baos);
        }

        return baos.toByteArray();
    }

    /**
     * Obtiene el formato de imagen del archivo
     */
    private String obtenerFormatoImagen(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null) {
            if (contentType.contains("jpeg") || contentType.contains("jpg"))
                return "jpg";
            if (contentType.contains("png"))
                return "png";
            if (contentType.contains("webp"))
                return "webp";
            if (contentType.contains("avif"))
                return "avif";
        }
        return "jpg"; // Default
    }

    /**
     * Obtiene la extensión del archivo
     */
    private String obtenerExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Verifica si la extensión del archivo es válida
     */
    private boolean tieneExtensionValida(String filename) {
        String extension = obtenerExtension(filename);
        return Arrays.asList("jpg", "jpeg", "png", "webp", "avif").contains(extension);
    }
}
