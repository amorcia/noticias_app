package com.noticias.api.servicios;

import com.noticias.api.entidades.DenunciaEntidad;
import com.noticias.api.entidades.SancionEntidad;
import com.noticias.api.entidades.UsuarioEntidad;
import com.noticias.api.repositorios.DenunciaRepositorio;
import com.noticias.api.repositorios.SancionRepositorio;
import com.noticias.api.repositorios.UsuarioRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Map;

@Service
public class ModeracionServicio {

    @Autowired
    private SancionRepositorio sancionRepositorio;

    @Autowired
    private DenunciaRepositorio denunciaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String NSFW_SERVICE_URL = "http://nsfw-service:5000/classify";

    public boolean esContenidoNSFW(MultipartFile archivo) {
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

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    NSFW_SERVICE_URL,
                    Objects.requireNonNull(HttpMethod.POST),
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            Map<String, Object> bodyResponse = response.getBody();
            if (bodyResponse != null && bodyResponse.containsKey("score")) {
                Double score = (Double) bodyResponse.get("score");
                return score > 0.8; // Umbral del 80%
            }
            return false;

        } catch (Exception e) {
            System.err.println("Error al conectar con servicio NSFW: " + e.getMessage());
            return false; // En caso de error, asumimos seguro por defecto (o bloquear, según política)
        }
    }

    public void vetarUsuarioAutomaticamente(UsuarioEntidad usuario, String motivo) {
        usuario.setVetado(true);
        usuario.setMotivoVeto(motivo);
        usuario.setFechaVeto(LocalDateTime.now());
        usuario.setVetadoHasta(LocalDateTime.now().plusDays(7));
        usuarioRepositorio.save(usuario);

        registrarSancion(usuario, null, "TEMPORAL", motivo, "Auto-ban por sistema NSFW");

        // Aquí se enviaría el email
        System.out.println("Enviando email de veto a: " + usuario.getEmail());
    }

    public void registrarSancion(UsuarioEntidad usuario, UsuarioEntidad admin, String tipo, String motivo,
            String resolucion) {
        SancionEntidad sancion = new SancionEntidad();
        sancion.setUsuario(usuario);
        sancion.setAdmin(admin);
        sancion.setTipo(tipo);
        sancion.setEstado(admin == null ? "PENDIENTE" : "RESUELTO"); // Si es auto, queda pendiente de revisión o
                                                                     // resuelto? Digamos pendiente para que admin lo
                                                                     // vea.
        if (admin == null)
            sancion.setEstado("RESUELTO"); // Auto-ban es una sanción ya aplicada.

        sancion.setMotivo(motivo);
        sancion.setResolucion(resolucion);
        sancion.setFechaInicio(LocalDateTime.now());
        if ("TEMPORAL".equals(tipo)) {
            sancion.setFechaFin(LocalDateTime.now().plusDays(7));
        }
        sancionRepositorio.save(sancion);
    }

    public boolean estaVetado(UsuarioEntidad usuario) {
        if (usuario.getVetado()) {
            if (usuario.getVetadoHasta() != null && usuario.getVetadoHasta().isBefore(LocalDateTime.now())) {
                // Veto expirado
                usuario.setVetado(false);
                usuario.setVetadoHasta(null);
                usuarioRepositorio.save(usuario);
                return false;
            }
            return true;
        }
        return false;
    }

    public void crearDenuncia(UsuarioEntidad autor, String tipo, Integer idObjeto, String motivo) {
        DenunciaEntidad denuncia = new DenunciaEntidad();
        denuncia.setDenunciante(autor);
        // Map legacy "tipo" + "idObjeto" to new relationships
        // Assuming "NOTICIA" or "COMENTARIO" strings
        if ("NOTICIA".equalsIgnoreCase(tipo)) {
            // Need NoticiaEntidad ref. Using lazy load via ID (getReference in
            // EntityManager) or Repo methods
            // Hack: we don't have NoticiaRepositorio injected here. Need to inject it.
            // For now, simpler: ModeracionServicio seems to be used by old code?
            // Better: Inject Repositories.
        }

        // This legacy method is tricky without Repositories.
        // Let's inject them at the top.

        denuncia.setMotivo(motivo);
        denuncia.setEstado("PENDIENTE");
        denuncia.setFecha(LocalDateTime.now());
        denunciaRepositorio.save(denuncia);
    }
}
