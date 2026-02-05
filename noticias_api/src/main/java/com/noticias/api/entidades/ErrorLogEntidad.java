package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad para tracking de errores con mensajes técnicos y amigables
 */
@Entity
@Table(name = "error_logs")
public class ErrorLogEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "el_id")
    private Long id;

    @Column(name = "el_usuario_id")
    private Integer usuarioId;

    @Column(name = "el_usuario_nombre")
    private String usuarioNombre;

    @Column(name = "el_tipo_error", nullable = false, length = 50)
    private String tipoError; // VALIDATION, DATABASE, NETWORK, SECURITY, UNKNOWN

    // MENSAJE TÉCNICO (para logs y admin)
    @Column(name = "el_mensaje_tecnico", columnDefinition = "TEXT", nullable = false)
    private String mensajeTecnico;

    // MENSAJE AMIGABLE (mostrado al usuario)
    @Column(name = "el_mensaje_usuario", columnDefinition = "TEXT")
    private String mensajeUsuario;

    @Column(name = "el_stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "el_clase")
    private String clase;

    @Column(name = "el_metodo")
    private String metodo;

    @Column(name = "el_paquete")
    private String paquete;

    @Column(name = "el_endpoint", length = 500)
    private String endpoint;

    @Column(name = "el_metodo_http", length = 10)
    private String metodoHttp;

    @Column(name = "el_timestamp")
    private LocalDateTime timestamp;

    @Column(name = "el_ultima_actividad")
    private LocalDateTime ultimaActividad;

    @Column(name = "el_ip_address", length = 45)
    private String ipAddress;

    public ErrorLogEntidad() {
        this.timestamp = LocalDateTime.now();
        this.ultimaActividad = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getTipoError() {
        return tipoError;
    }

    public void setTipoError(String tipoError) {
        this.tipoError = tipoError;
    }

    public String getMensajeTecnico() {
        return mensajeTecnico;
    }

    public void setMensajeTecnico(String mensajeTecnico) {
        this.mensajeTecnico = mensajeTecnico;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }

    public void setMensajeUsuario(String mensajeUsuario) {
        this.mensajeUsuario = mensajeUsuario;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getClase() {
        return clase;
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getPaquete() {
        return paquete;
    }

    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMetodoHttp() {
        return metodoHttp;
    }

    public void setMetodoHttp(String metodoHttp) {
        this.metodoHttp = metodoHttp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getUltimaActividad() {
        return ultimaActividad;
    }

    public void setUltimaActividad(LocalDateTime ultimaActividad) {
        this.ultimaActividad = ultimaActividad;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
