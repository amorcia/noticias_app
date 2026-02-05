package com.noticias.api.servicios;

import com.noticias.api.entidades.ErrorLogEntidad;
import com.noticias.api.repositorios.ErrorLogRepositorio;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

/**
 * Servicio para logging de errores en base de datos
 */
@Service
public class ErrorLogServicio {

    private final ErrorLogRepositorio errorLogRepositorio;

    public ErrorLogServicio(ErrorLogRepositorio errorLogRepositorio) {
        this.errorLogRepositorio = errorLogRepositorio;
    }

    /**
     * Registra un error en base de datos
     */
    @Async
    @Transactional
    public void logError(Exception e, Integer usuarioId, String usuarioNombre, String endpoint, String metodoHttp) {
        if (e == null) {
            return;
        }

        try {
            ErrorLogEntidad log = new ErrorLogEntidad();
            log.setUsuarioId(usuarioId);
            log.setUsuarioNombre(usuarioNombre);
            log.setTipoError(clasificarError(e));
            log.setMensajeTecnico(generarMensajeTecnico(e));
            log.setMensajeUsuario(generarMensajeAmigable(e));
            log.setStackTrace(obtenerStackTrace(e));
            log.setEndpoint(endpoint);
            log.setMetodoHttp(metodoHttp);

            if (e.getStackTrace().length > 0) {
                StackTraceElement elemento = e.getStackTrace()[0];
                log.setClase(elemento.getClassName());
                log.setMetodo(elemento.getMethodName());
                log.setPaquete(extraerPaquete(elemento.getClassName()));
            }

            errorLogRepositorio.save(log);

        } catch (Exception ex) {
            // No lanzar excepción para no interrumpir el flujo
            System.err.println("Error guardando log en BD: " + ex.getMessage());
        }
    }

    /**
     * Genera mensaje amigable para el usuario
     */
    public String generarMensajeAmigable(Exception e) {
        if (e == null) {
            return "Ha ocurrido un error inesperado. Por favor, inténtalo de nuevo.";
        }

        String nombreExcepcion = e.getClass().getSimpleName();

        if (nombreExcepcion.contains("NotFound") || nombreExcepcion.contains("NoSuchElement")) {
            return "El recurso solicitado no se encontró. Por favor, verifica e inténtalo de nuevo.";
        } else if (nombreExcepcion.contains("AccessDenied") || nombreExcepcion.contains("Forbidden")) {
            return "No tienes permisos para realizar esta acción.";
        } else if (nombreExcepcion.contains("BadRequest") || nombreExcepcion.contains("IllegalArgument")) {
            return "Los datos proporcionados no son válidos. Por favor, revisa la información e inténtalo de nuevo.";
        } else if (nombreExcepcion.contains("Conflict") || nombreExcepcion.contains("Duplicate")) {
            return "Ya existe un registro con esos datos. Por favor, verifica e inténtalo de nuevo.";
        } else if (nombreExcepcion.contains("Timeout")) {
            return "La operación tardó demasiado tiempo. Por favor, inténtalo de nuevo.";
        } else if (nombreExcepcion.contains("Database") || nombreExcepcion.contains("SQL")) {
            return "Error al procesar la información. Por favor, inténtalo de nuevo más tarde.";
        }

        return "Ha ocurrido un error. Por favor, inténtalo de nuevo o contacta con el administrador.";
    }

    /**
     * Genera mensaje técnico para logs
     */
    private String generarMensajeTecnico(Exception e) {
        if (e == null) {
            return "Unknown error";
        }

        String nombreExcepcion = e.getClass().getName();
        String mensaje = e.getMessage() != null ? e.getMessage() : "No message";
        String ubicacion = "";

        if (e.getStackTrace().length > 0) {
            StackTraceElement elemento = e.getStackTrace()[0];
            ubicacion = String.format(" en %s.%s línea %d",
                    elemento.getClassName(),
                    elemento.getMethodName(),
                    elemento.getLineNumber());
        }

        return nombreExcepcion + ": " + mensaje + ubicacion;
    }

    /**
     * Clasifica el tipo de error
     */
    private String clasificarError(Exception e) {
        if (e == null) {
            return "UNKNOWN";
        }

        String nombreExcepcion = e.getClass().getSimpleName().toUpperCase();

        if (nombreExcepcion.contains("VALIDATION") || nombreExcepcion.contains("ILLEGALARGUMENT")) {
            return "VALIDATION";
        } else if (nombreExcepcion.contains("DATABASE") || nombreExcepcion.contains("SQL")
                || nombreExcepcion.contains("PERSISTENCE")) {
            return "DATABASE";
        } else if (nombreExcepcion.contains("NETWORK") || nombreExcepcion.contains("TIMEOUT")
                || nombreExcepcion.contains("CONNECTION")) {
            return "NETWORK";
        } else if (nombreExcepcion.contains("SECURITY") || nombreExcepcion.contains("ACCESS")
                || nombreExcepcion.contains("FORBIDDEN")) {
            return "SECURITY";
        }

        return "UNKNOWN";
    }

    /**
     * Obtiene el stack trace completo como string
     */
    private String obtenerStackTrace(Exception e) {
        if (e == null) {
            return "";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Extrae el paquete de un nombre de clase completo
     */
    private String extraerPaquete(String nombreClaseCompleto) {
        if (nombreClaseCompleto == null || !nombreClaseCompleto.contains(".")) {
            return "";
        }
        int ultimoPunto = nombreClaseCompleto.lastIndexOf('.');
        return nombreClaseCompleto.substring(0, ultimoPunto);
    }

    /**
     * Limpia logs de error antiguos (>30 días desde última actividad)
     * Se ejecuta diariamente a las 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void limpiarLogsAntiguos() {
        try {
            LocalDateTime fechaLimite = LocalDateTime.now().minusDays(30);
            errorLogRepositorio.eliminarLogsAntiguos(fechaLimite);
        } catch (Exception e) {
            System.err.println("Error limpiando logs antiguos: " + e.getMessage());
        }
    }
}
