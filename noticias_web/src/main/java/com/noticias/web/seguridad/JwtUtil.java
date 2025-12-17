package com.noticias.web.seguridad;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final long expiracionMs;
    private final Key claveGlobal;

    public JwtUtil(@Value("${jwt.expiracionMs}") long expiracionMs) {
        this.expiracionMs = expiracionMs;
        // Clave global para tokens simples
        this.claveGlobal = Keys.hmacShaKeyFor(
                "noticias-app-secret-key-2024-muy-segura-y-larga".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String generarToken(Integer id, String email, String rol) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .claim("id", id)
                .claim("email", email)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(claveGlobal, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(claveGlobal).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extraerEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(claveGlobal).build().parseClaimsJws(token).getBody();
            return claims.get("email", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public String generarTokenConClavePersonal(Integer id, String email, String rol, String clavePersonal) {
        Key clave = Keys.hmacShaKeyFor(clavePersonal.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .claim("id", id)
                .claim("email", email)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims obtenerClaimsConClave(String token, Key clave) {
        return Jwts.parserBuilder().setSigningKey(clave).build().parseClaimsJws(token).getBody();
    }
}
