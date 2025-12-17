package com.noticias.web.dtos;

import java.time.LocalDateTime;

public class UsuarioApiDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String password; // hash
    private Integer rolId;
    private String rolNombre;
    private Boolean activo;

    private String tokenSecret;

    private String codigoRecuperacion;
    private LocalDateTime fechaExpiracionCodigo;

    public UsuarioApiDTO() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getRolId() { return rolId; }
    public void setRolId(Integer rolId) { this.rolId = rolId; }

    public String getRolNombre() { return rolNombre; }
    public void setRolNombre(String rolNombre) { this.rolNombre = rolNombre; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getTokenSecret() { return tokenSecret; }
    public void setTokenSecret(String tokenSecret) { this.tokenSecret = tokenSecret; }

    public String getCodigoRecuperacion() { return codigoRecuperacion; }
    public void setCodigoRecuperacion(String codigoRecuperacion) {
        this.codigoRecuperacion = codigoRecuperacion;
    }

    public LocalDateTime getFechaExpiracionCodigo() { return fechaExpiracionCodigo; }
    public void setFechaExpiracionCodigo(LocalDateTime fechaExpiracionCodigo) {
        this.fechaExpiracionCodigo = fechaExpiracionCodigo;
    }
}
