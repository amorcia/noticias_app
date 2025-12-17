package com.noticias.api.servicios;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

@Service
public class NsfwServicio {

    private final RestTemplate restTemplate;
    private final String nsfwServiceUrl = "http://nsfw-service:5000/predict";

    public NsfwServicio() {
        this.restTemplate = new RestTemplate();
    }

    public boolean esContenidoInapropiado(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return false;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(archivo.getBytes()) {
                @Override
                public String getFilename() {
                    return archivo.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(nsfwServiceUrl, requestEntity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                // Asumiendo que el servicio devuelve probabilidades.
                // Ajustar segun respuesta real del contenedor eugencepoi/nsfw_api
                // Generalmente devuelve JSON con clases y probabilidades.
                // Si "Porn" o "Hentai" o "Sexy" > 0.6 -> Inapropiado.

                double pornProb = root.path("Porn").asDouble(0.0);
                double hentaiProb = root.path("Hentai").asDouble(0.0);
                double sexyProb = root.path("Sexy").asDouble(0.0);

                return (pornProb > 0.6 || hentaiProb > 0.6 || sexyProb > 0.7);
            }

        } catch (IOException e) {
            e.printStackTrace();
            // En caso de error de conexión, por seguridad podríamos considerar "true" o
            // "false".
            // Para no bloquear funcionalidad por error técnico, devolvemos false pero
            // logueamos.
            return false;
        }

        return false;
    }
}
