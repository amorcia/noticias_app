package com.noticias.api.entidades;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Entidad Usuario para el sistema de Noticias.
 * Incluye campos para autenticación, tokens de confirmación/recuperación,
 * y sistema de veto/ban.
 */
@Entity
@Table(name = "usuarios")
public class UsuarioEntidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usu_id")
    private Integer id;

    @Column(name = "usu_nombre_completo", nullable = false)
    private String nombreCompleto;

    @Column(name = "usu_email", nullable = false, unique = true)
    private String email;

    @Column(name = "usu_movil")
    private String movil;

    @Column(name = "usu_password", nullable = false)
    private String password; // hash bcrypt

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private RolEntidad rol;

    @Column(name = "usu_activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "usu_codigo_verificacion", length = 255)
    private String codigoVerificacion;

    @Column(name = "usu_token_session", length = 512)
    private String tokenSession;

    // Campos para el sistema de Veto/Ban
    @Column(name = "usu_vetado", nullable = false)
    private Boolean vetado = false;

    @Column(name = "usu_motivo_veto", columnDefinition = "TEXT")
    private String motivoVeto;

    @Column(name = "usu_fecha_veto")
    private LocalDateTime fechaVeto;

    @Column(name = "usu_vetado_hasta")
    private LocalDateTime vetadoHasta;

    @Column(name = "usu_secret_key_2fa")
    private String secretKey2FA;

    @Column(name = "usu_imagen_url")
    private String imagenUrl;

    @Column(name = "usu_email_pendiente")
    private String emailPendiente;

    public UsuarioEntidad() {
    }

    public String getSecretKey2FA() {
        return secretKey2FA;
    }

    public void setSecretKey2FA(String secretKey2FA) {
        this.secretKey2FA = secretKey2FA;
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

    // Getters y setters
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

    public RolEntidad getRol() {
        return rol;
    }

    public void setRol(RolEntidad rol) {
        this.rol = rol;
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

}
