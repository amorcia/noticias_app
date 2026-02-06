package com.noticias.web.servicios;

import com.noticias.web.dtos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente para comunicarse con la API de datos (noticias_api).
 * Proporciona métodos para todas las operaciones CRUD de las entidades.
 */
@Service
@SuppressWarnings({ "null", "unchecked" })
public class ApiNoticiasCliente {

    @Value("${api.noticias.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    public ApiNoticiasCliente() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos
        factory.setReadTimeout(5000); // 5 segundos
        this.restTemplate = new RestTemplate(factory);
    }

    // ==================== USUARIOS ====================

    public List<UsuarioDTO> listarUsuarios() {
        String url = apiUrl + "/usuarios";
        ResponseEntity<List<UsuarioDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<UsuarioDTO>>() {
                });
        return response.getBody();
    }

    public UsuarioDTO buscarUsuarioPorEmail(String email) {
        String url = apiUrl + "/usuarios/email/" + email;
        try {
            System.out.println("🔍 Buscando usuario por email: " + url);
            return restTemplate.getForObject(url, UsuarioDTO.class);
        } catch (Exception e) {
            System.out.println("❌ Error buscando usuario por email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public UsuarioDTO buscarUsuarioPorId(Integer id) {
        String url = apiUrl + "/usuarios/" + id;
        try {
            return restTemplate.getForObject(url, UsuarioDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public UsuarioDTO crearUsuario(UsuarioDTO usuario) {
        String url = apiUrl + "/usuarios";
        return restTemplate.postForObject(url, usuario, UsuarioDTO.class);
    }

    public UsuarioDTO actualizarUsuario(Integer id, UsuarioDTO usuario) {
        String url = apiUrl + "/usuarios/" + id;
        HttpEntity<UsuarioDTO> request = new HttpEntity<>(usuario);
        ResponseEntity<UsuarioDTO> response = restTemplate.exchange(url, HttpMethod.PUT, request, UsuarioDTO.class);
        return response.getBody();
    }

    public boolean vetarUsuario(Integer id, String motivo, String duracion) {
        String url = apiUrl + "/usuarios/" + id + "/vetar";
        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("motivo", motivo);
        if (duracion != null)
            payload.put("duracion", duracion);

        try {
            restTemplate.postForObject(url, payload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean desvetarUsuario(Integer id) {
        String url = apiUrl + "/usuarios/" + id + "/desvetar";
        try {
            restTemplate.postForObject(url, null, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean cerrarSesion(Integer id) {
        String url = apiUrl + "/usuarios/" + id + "/logout";
        try {
            restTemplate.postForObject(url, null, Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarAvatar(Integer id) {
        String url = apiUrl + "/usuarios/" + id + "/imagen";
        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean desactivar2FA(Integer id) {
        String url = apiUrl + "/usuarios/" + id + "/disable-2fa";
        try {
            restTemplate.postForObject(url, null, Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, String> subirAvatar(Integer id, org.springframework.web.multipart.MultipartFile file) {
        String url = apiUrl + "/usuarios/" + id + "/imagen";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                    headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean actualizarTokenSesion(Integer id, String token) {
        String url = apiUrl + "/usuarios/" + id + "/session-token";
        Map<String, String> payload = new java.util.HashMap<>();
        payload.put("token", token);
        try {
            restTemplate.postForObject(url, payload, Void.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== CATEGORÍAS ====================

    public List<CategoriaDTO> listarCategorias() {
        String url = apiUrl + "/categorias";
        System.out.println("🔍 Llamando a API de categorías: " + url);
        try {
            ResponseEntity<List<CategoriaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<CategoriaDTO>>() {
                    });
            List<CategoriaDTO> categorias = response.getBody();
            System.out.println("✅ Respuesta de categorías recibida: "
                    + (categorias != null ? categorias.size() + " categorías" : "null"));
            if (categorias != null && !categorias.isEmpty()) {
                System.out.println("📋 Primera categoría: " + categorias.get(0).getNombre());
            }
            return categorias;
        } catch (Exception e) {
            System.out.println("❌ Error al obtener categorías: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    public List<CategoriaDTO> listarCategoriasRaiz() {
        String url = apiUrl + "/categorias/raiz";
        ResponseEntity<List<CategoriaDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<CategoriaDTO>>() {
                });
        return response.getBody();
    }

    public List<CategoriaDTO> listarSubcategorias(Integer categoriaId) {
        String url = apiUrl + "/categorias/" + categoriaId + "/subcategorias";
        ResponseEntity<List<CategoriaDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<CategoriaDTO>>() {
                });
        return response.getBody();
    }

    public CategoriaDTO buscarCategoriaPorId(Integer id) {
        String url = apiUrl + "/categorias/" + id;
        try {
            return restTemplate.getForObject(url, CategoriaDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public CategoriaDTO buscarCategoriaPorNombre(String nombre) {
        String url = apiUrl + "/categorias/nombre/" + nombre;
        try {
            return restTemplate.getForObject(url, CategoriaDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== NOTICIAS ====================

    public List<NoticiaDTO> listarTodasLasNoticias() {
        String url = apiUrl + "/noticias";
        ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<NoticiaDTO>>() {
                });
        return response.getBody();
    }

    public List<NoticiaDTO> listarNoticias() {
        return listarTodasLasNoticias();
    }

    public List<NoticiaDTO> listarNoticiasDestacadas() {
        String url = apiUrl + "/noticias/destacadas";
        ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<NoticiaDTO>>() {
                });
        return response.getBody();
    }

    public List<NoticiaDTO> listarNoticiasPopulares() {
        String url = apiUrl + "/noticias/populares";
        ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<NoticiaDTO>>() {
                });
        return response.getBody();
    }

    public List<NoticiaDTO> listarNoticiasPorCategoria(Integer categoriaId) {
        String url = apiUrl + "/noticias/categoria/" + categoriaId;
        try {
            ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<NoticiaDTO> listarNoticiasPorCategoriaFiltrado(Integer categoriaId, String filtro, Integer mes,
            Integer anio) {
        String url = apiUrl + "/noticias/categoria/" + categoriaId + "/filtrar?";
        if (filtro != null)
            url += "filtro=" + filtro + "&";
        if (mes != null)
            url += "mes=" + mes + "&";
        if (anio != null)
            url += "anio=" + anio;

        try {
            ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<NoticiaDTO> listarNoticiasPorCategoriaNombre(String nombre) {
        String url = apiUrl + "/noticias/categoria/nombre/" + nombre;
        try {
            ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<NoticiaDTO> listarNoticiasForoPorCategoriaNombre(String nombre) {
        String url = apiUrl + "/noticias/categoria/nombre/" + nombre + "/foro";
        try {
            ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<NoticiaDTO> listarNoticiasPorAutor(Integer autorId) {
        String url = apiUrl + "/noticias/autor/" + autorId;
        try {
            ResponseEntity<List<NoticiaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            return List.of();
        }
    }

    public NoticiaDTO buscarNoticiaPorId(Integer id) {
        String url = apiUrl + "/noticias/" + id;
        try {
            return restTemplate.getForObject(url, NoticiaDTO.class);
        } catch (Exception e) {
            System.out.println("❌ Error buscando noticia por ID: " + e.getMessage());
            return null;
        }
    }

    public NoticiaDTO buscarNoticiaPorTitulo(String titulo) {
        String encodedTitulo = java.net.URLEncoder.encode(titulo, java.nio.charset.StandardCharsets.UTF_8);
        String url = apiUrl + "/noticias/titulo?titulo=" + encodedTitulo;
        try {
            return restTemplate.getForObject(url, NoticiaDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    public NoticiaDTO crearNoticia(NoticiaDTO noticia) {
        String url = apiUrl + "/noticias";
        return restTemplate.postForObject(url, noticia, NoticiaDTO.class);
    }

    public NoticiaDTO actualizarNoticia(Integer id, NoticiaDTO noticia) {
        String url = apiUrl + "/noticias/" + id;
        HttpEntity<NoticiaDTO> request = new HttpEntity<>(noticia);
        ResponseEntity<NoticiaDTO> response = restTemplate.exchange(url, HttpMethod.PUT, request, NoticiaDTO.class);
        return response.getBody();
    }

    public void votarNoticia(Integer id, boolean like, Integer usuarioId) {
        String url = apiUrl + "/noticias/" + id + "/votar?like=" + like + "&usuarioId=" + usuarioId;
        restTemplate.postForObject(url, null, Void.class);
    }

    public String obtenerVotoUsuario(Integer noticiaId, Integer usuarioId) {
        String url = apiUrl + "/noticias/" + noticiaId + "/voto?usuarioId=" + usuarioId;
        try {
            Map<String, String> response = restTemplate.getForObject(url, Map.class);
            return response != null ? response.get("tipo") : "NONE";
        } catch (Exception e) {
            return "NONE";
        }
    }

    public List<NoticiaEliminadaDTO> listarNoticiasEliminadas() {
        String url = apiUrl + "/noticias/eliminadas/admin";
        try {
            ResponseEntity<List<NoticiaEliminadaDTO>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<NoticiaEliminadaDTO>>() {
                    });
            return response.getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean eliminarNoticia(Integer id, String motivo, Integer eliminadorId) {
        String url = apiUrl + "/noticias/" + id + "?";
        if (motivo != null)
            url += "motivo=" + motivo + "&";
        if (eliminadorId != null)
            url += "eliminadorId=" + eliminadorId;

        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarNoticiaConConfirmacion(Integer id, String titulo, Integer usuarioId) {
        String url = apiUrl + "/noticias/" + id + "/eliminar-con-confirmacion";
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("tituloConfirmacion", titulo);
        payload.put("usuarioId", usuarioId);

        try {
            restTemplate.postForObject(url, payload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarNoticiaConJustificacion(Integer id, String motivo, String descripcion,
            Integer eliminadorId) {
        String url = apiUrl + "/noticias/" + id + "/eliminar-con-justificacion";
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("motivo", motivo);
        payload.put("descripcion", descripcion);
        payload.put("eliminadorId", eliminadorId);

        try {
            restTemplate.postForObject(url, payload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean eliminarNoticiaPorTitulo(String titulo, String motivo, String descripcion, Integer eliminadorId) {
        // Encode titulo
        String encodedTitulo = java.net.URLEncoder.encode(titulo, java.nio.charset.StandardCharsets.UTF_8);
        String url = apiUrl + "/noticias/titulo?titulo=" + encodedTitulo;
        if (motivo != null)
            url += "&motivo=" + java.net.URLEncoder.encode(motivo, java.nio.charset.StandardCharsets.UTF_8);
        if (descripcion != null)
            url += "&descripcion=" + java.net.URLEncoder.encode(descripcion, java.nio.charset.StandardCharsets.UTF_8);
        if (eliminadorId != null)
            url += "&eliminadorId=" + eliminadorId;

        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enviarDenuncia(Integer noticiaId, Integer comentarioId, Integer usuarioId, String motivo,
            String descripcion) {
        String url = apiUrl + "/interacciones/denuncias";
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        if (noticiaId != null)
            payload.put("noticiaId", noticiaId);
        if (comentarioId != null)
            payload.put("comentarioId", comentarioId);
        payload.put("usuarioId", usuarioId);
        payload.put("motivo", motivo);
        payload.put("descripcion", descripcion != null ? descripcion : "");

        try {
            restTemplate.postForObject(url, payload, String.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<com.noticias.web.dtos.DenunciaDTO> listarDenuncias() {
        try {
            return restTemplate.exchange(
                    apiUrl + "/interacciones/denuncias",
                    org.springframework.http.HttpMethod.GET,
                    null,
                    new org.springframework.core.ParameterizedTypeReference<java.util.List<com.noticias.web.dtos.DenunciaDTO>>() {
                    }).getBody();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    public List<ComentarioDTO> listarComentariosPorNoticia(Integer noticiaId, Integer usuarioId) {
        String url = apiUrl + "/interacciones/comentarios/noticia/" + noticiaId;
        if (usuarioId != null) {
            url += "?usuarioId=" + usuarioId;
        }
        ResponseEntity<List<ComentarioDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<ComentarioDTO>>() {
                });
        return response.getBody();
    }

    public ComentarioDTO crearComentario(ComentarioDTO comentario) {
        String url = apiUrl + "/interacciones/comentarios";

        // Match API payload expectations
        java.util.Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("noticiaId", comentario.getNoticiaId());
        payload.put("usuarioId", comentario.getAutor().getId());
        payload.put("contenido", comentario.getContenido());
        if (comentario.getPadreId() != null)
            payload.put("padreId", comentario.getPadreId());

        return restTemplate.postForObject(url, payload, ComentarioDTO.class);
    }

    public void votarComentario(Integer id, boolean like, Integer usuarioId) {
        String url = apiUrl + "/interacciones/comentarios/" + id + "/votar?like=" + like + "&usuarioId=" + usuarioId;
        restTemplate.postForObject(url, null, Void.class);
    }

    public boolean eliminarComentario(Integer id, Integer usuarioId, boolean esAdmin) {
        String url = apiUrl + "/interacciones/comentarios/" + id + "?usuarioId=" + usuarioId + "&esAdmin=" + esAdmin;
        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== AUTENTICACIÓN ====================

    public boolean confirmarEmail(String token) {
        String url = apiUrl + "/usuarios/confirmar-email";
        Map<String, String> payload = Map.of("token", token);
        try {
            restTemplate.postForObject(url, payload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String generarTokenRecuperacion(String email) {
        String url = apiUrl + "/usuarios/recuperar-password";
        Map<String, String> payload = Map.of("email", email);
        try {
            Map<String, String> response = restTemplate.postForObject(url, payload, Map.class);
            return response != null ? response.get("token") : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean restablecerPassword(String token, String nuevaPasswordEncriptada) {
        String url = apiUrl + "/usuarios/restablecer-password";
        Map<String, String> payload = Map.of("token", token, "nuevaPassword", nuevaPasswordEncriptada);
        try {
            restTemplate.postForObject(url, payload, Map.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ==================== PUBLICACIÓN USUARIO ====================

    public NoticiaDTO publicarNoticiaUsuario(String titulo, String subtitulo, String contenido,
            Integer categoriaId, Integer autorId,
            org.springframework.web.multipart.MultipartFile file) {
        String url = apiUrl + "/noticias/publicar";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("titulo", titulo);
            body.add("subtitulo", subtitulo);
            body.add("contenido", contenido);
            body.add("categoriaId", categoriaId);
            body.add("autorId", autorId);
            body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                    headers);

            ResponseEntity<NoticiaDTO> response = restTemplate.postForEntity(url, requestEntity, NoticiaDTO.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error al publicar noticia: " + e.getMessage());
        }
    }

    // ==================== ADMIN & INTERACCIONES ====================

    public List<SancionDTO> listarSanciones() {
        String url = apiUrl + "/admin/sanciones";
        ResponseEntity<List<SancionDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<SancionDTO>>() {
                });
        return response.getBody();
    }

    public void resolverSancion(Integer id, String resolucion, String accion, Integer adminId) {
        String url = apiUrl + "/admin/sanciones/" + id + "/resolver?resolucion=" + resolucion + "&accion=" + accion
                + "&adminId=" + adminId;
        restTemplate.postForObject(url, null, Map.class);
    }

    public List<UsuarioDTO> listarVetados() {
        String url = apiUrl + "/admin/vetados";
        ResponseEntity<List<UsuarioDTO>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<UsuarioDTO>>() {
                });
        return response.getBody();
    }

    public Map<String, Object> getAdminStats() {
        String url = apiUrl + "/admin/stats";
        try {
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("totalUsuarios", 0, "usuariosVetados", 0, "porcentajeVetados", 0, "totalNoticias", 0);
        }
    }
    // ==================== NOTICIAS MULTIPART ====================

    public String publicarNoticia(String titulo, String subtitulo, String contenido, Integer categoriaId,
            Integer autorId, org.springframework.web.multipart.MultipartFile file) {
        String url = apiUrl + "/noticias/publicar";

        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("titulo", titulo);
            body.add("subtitulo", subtitulo);
            body.add("contenido", contenido);
            body.add("categoriaId", categoriaId);
            body.add("autorId", autorId);

            if (file != null && !file.isEmpty()) {
                body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }

            org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new org.springframework.http.HttpEntity<>(
                    body, headers);

            restTemplate.postForEntity(url, requestEntity, String.class);
            return null; // Éxito
        } catch (org.springframework.web.client.RestClientResponseException e) {
            return e.getResponseBodyAsString(); // Error del servidor (4xx o 5xx)
        } catch (Exception e) {
            e.printStackTrace();
            return "Error interno al conectar con el servidor: " + e.getMessage();
        }
    }

    public boolean resolverDenuncia(Integer id, String estado) {
        String url = apiUrl + "/admin/denuncias/" + id + "/resolver?estado=" + estado;
        try {
            restTemplate.postForObject(url, null, Map.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarUsuario(Integer id) {
        String url = apiUrl + "/usuarios/" + id;
        try {
            restTemplate.delete(url);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarUsuarioConJustificacion(Integer id, String motivo, String descripcion,
            Integer eliminadorId) {
        String url = apiUrl + "/usuarios/" + id + "/eliminar-con-justificacion";
        Map<String, Object> payload = new java.util.HashMap<>();
        payload.put("motivo", motivo);
        payload.put("descripcion", descripcion);
        payload.put("eliminadorId", eliminadorId);

        try {
            restTemplate.postForObject(url, payload, Void.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String editarNoticia(Integer id, String titulo, String subtitulo, String contenido, Integer categoriaId,
            org.springframework.web.multipart.MultipartFile file) {
        String url = apiUrl + "/noticias/" + id + "/editar";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
            body.add("titulo", titulo);
            body.add("subtitulo", subtitulo);
            body.add("contenido", contenido);
            body.add("categoriaId", categoriaId);

            if (file != null && !file.isEmpty()) {
                body.add("file", new org.springframework.core.io.ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }

            HttpEntity<org.springframework.util.MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body,
                    headers);

            restTemplate.postForObject(url, requestEntity, String.class);
            return null; // Éxito
        } catch (org.springframework.web.client.RestClientResponseException e) {
            return e.getResponseBodyAsString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
