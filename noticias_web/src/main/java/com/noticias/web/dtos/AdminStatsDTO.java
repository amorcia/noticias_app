package com.noticias.web.dtos;

import java.util.Map;

public class AdminStatsDTO {
    private long totalUsuarios;
    private long usuariosVetados;
    private double porcentajeVetados;
    private long totalNoticias;
    private long totalSanciones;
    private long sancionesPendientes;

    public AdminStatsDTO() {
    }

    public AdminStatsDTO(Map<String, Object> statsMap) {
        if (statsMap != null) {
            this.totalUsuarios = parseLong(statsMap.get("totalUsuarios"));
            this.usuariosVetados = parseLong(statsMap.get("usuariosVetados"));
            this.porcentajeVetados = parseDouble(statsMap.get("porcentajeVetados"));
            this.totalNoticias = parseLong(statsMap.get("totalNoticias"));
            this.totalSanciones = parseLong(statsMap.get("totalSanciones"));
            this.sancionesPendientes = parseLong(statsMap.get("sancionesPendientes"));
        }
    }

    private long parseLong(Object obj) {
        if (obj instanceof Number)
            return ((Number) obj).longValue();
        return 0L;
    }

    private double parseDouble(Object obj) {
        if (obj instanceof Number)
            return ((Number) obj).doubleValue();
        return 0.0;
    }

    // Getters and Setters
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getUsuariosVetados() {
        return usuariosVetados;
    }

    public void setUsuariosVetados(long usuariosVetados) {
        this.usuariosVetados = usuariosVetados;
    }

    public double getPorcentajeVetados() {
        return porcentajeVetados;
    }

    public void setPorcentajeVetados(double porcentajeVetados) {
        this.porcentajeVetados = porcentajeVetados;
    }

    public long getTotalNoticias() {
        return totalNoticias;
    }

    public void setTotalNoticias(long totalNoticias) {
        this.totalNoticias = totalNoticias;
    }

    public long getTotalSanciones() {
        return totalSanciones;
    }

    public void setTotalSanciones(long totalSanciones) {
        this.totalSanciones = totalSanciones;
    }

    public long getSancionesPendientes() {
        return sancionesPendientes;
    }

    public void setSancionesPendientes(long sancionesPendientes) {
        this.sancionesPendientes = sancionesPendientes;
    }
}
