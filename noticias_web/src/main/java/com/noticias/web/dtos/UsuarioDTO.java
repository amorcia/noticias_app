package com.noticias.web.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public class UsuarioDTO {
    private Integer id;
    private String nombreCompleto;
    private String email;
    private String movil;
    private String password;
    private Integer rolId;
    private String rolNombre;
    private Boolean activo;
    private String codigoVerificacion;
    private String tokenSession;
    private Boolean vetado;
    private String motivoVeto;
    private String secretKey2FA;
    private Integer rolNivel; // 1: OWNER, 2: ADMIN, 3: TRABAJADOR, 4: USER
    private LocalDateTime fechaVeto;
    private LocalDateTime vetadoHasta;
    private String imagenUrl;
    private String emailPendiente;

    public UsuarioDTO() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRolId() {
        return rolId;
    }

    public void setRolId(Integer rolId) {
        this.rolId = rolId;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getCodigoVerificacion() {
        return codigoVerificacion;
    }

    public void setCodigoVerificacion(String codigoVerificacion) {
        this.codigoVerificacion = codigoVerificacion;
    }

    public String getTokenSession() {
        return tokenSession;
    }

    public void setTokenSession(String tokenSession) {
        this.tokenSession = tokenSession;
    }

    public Boolean getVetado() {
        return vetado;
    }

    public void setVetado(Boolean vetado) {
        this.vetado = vetado;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public void setMotivoVeto(String motivoVeto) {
        this.motivoVeto = motivoVeto;
    }

    public String getSecretKey2FA() {
        return secretKey2FA;
    }

    public void setSecretKey2FA(String secretKey2FA) {
        this.secretKey2FA = secretKey2FA;
    }

    public Integer getRolNivel() {
        return rolNivel;
    }

    public void setRolNivel(Integer rolNivel) {
        this.rolNivel = rolNivel;
    }

    public LocalDateTime getFechaVeto() {
        return fechaVeto;
    }

    public void setFechaVeto(LocalDateTime fechaVeto) {
        this.fechaVeto = fechaVeto;
    }

    public LocalDateTime getVetadoHasta() {
        return vetadoHasta;
    }

    public void setVetadoHasta(LocalDateTime vetadoHasta) {
        this.vetadoHasta = vetadoHasta;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getEmailPendiente() {
        return emailPendiente;
    }

    public void setEmailPendiente(String emailPendiente) {
        this.emailPendiente = emailPendiente;
    }

    // Helper methods for UI
    public String getRol() {
        return rolNombre != null ? rolNombre : "USUARIO";
    }

    public String getNombre() {
        return nombreCompleto;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("rol")
    public Map<String, Object> getRolObject() {
        if (rolId != null) {
            return Map.of("id", rolId, "nombre", rolNombre != null ? rolNombre : "USUARIO");
        }
        return null;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("rol")
    private void unpackRol(Map<String, Object> rol) {
        if (rol != null) {
            Object idObj = rol.get("id");
            if (idObj == null)
                idObj = rol.get("rol_id");
            if (idObj instanceof Integer)
                this.rolId = (Integer) idObj;

            String name = (String) rol.get("nombre");
            if (name == null)
                name = (String) rol.get("rol_nombre");
            if (name == null)
                name = (String) rol.get("rolNombre");

            if (name != null) {
                this.rolNombre = name.toUpperCase();
            } else {
                this.rolNombre = "USUARIO";
            }
        }
    }
}
