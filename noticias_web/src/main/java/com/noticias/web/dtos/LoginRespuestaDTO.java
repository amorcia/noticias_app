package com.noticias.web.dtos;

public class LoginRespuestaDTO {
    private String token;
    private UsuarioRespuestaDTO usuario;

    private boolean requiresReset;

    public LoginRespuestaDTO() {
    }

    public LoginRespuestaDTO(String token, UsuarioRespuestaDTO usuario, boolean requiresReset) {
        this.token = token;
        this.usuario = usuario;
        this.requiresReset = requiresReset;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioRespuestaDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioRespuestaDTO usuario) {
        this.usuario = usuario;
    }

    public boolean isRequiresReset() {
        return requiresReset;
    }

    public void setRequiresReset(boolean requiresReset) {
        this.requiresReset = requiresReset;
    }
}
