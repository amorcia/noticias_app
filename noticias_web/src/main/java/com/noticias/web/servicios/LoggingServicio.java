package com.noticias.web.servicios;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servicio centralizado para logging de sesiones y errores
 * - Logs de sesión: Archivos en /logs/sesiones/usuario_{id}.log
 * - Logs de error: Archivos en /logs/errores/
 */
@Service
public class LoggingServicio {

    private static final String LOGS_BASE_DIR = "logs";
    private static final String SESIONES_DIR = LOGS_BASE_DIR + "/sesiones";
    private static final String ERRORES_DIR = LOGS_BASE_DIR + "/errores";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Registra una acción de sesión en archivo
     * Formato: [TIMESTAMP] | ACCION | RUTA | METODO_HTTP | CONTROLADOR.METODO |
     * PAQUETE | RESULTADO | MENSAJE
     */
    @Async
    public void logSesionToFile(Integer usuarioId, String accion, String ruta,
            String metodoHttp, String controlador, String paquete,
            String resultado, String mensaje) {
        if (usuarioId == null) {
            return;
        }

        try {
            // Crear directorio si no existe
            Path sesionesPath = Paths.get(SESIONES_DIR);
            if (!Files.exists(sesionesPath)) {
                Files.createDirectories(sesionesPath);
            }

            // Archivo de log del usuario
            Path logFile = sesionesPath.resolve("usuario_" + usuarioId + ".log");

            // Formatear línea de log
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logLine = String.format("[%s] | %s | %s | %s | %s | %s | %s | %s%n",
                    timestamp,
                    accion != null ? accion : "UNKNOWN",
                    ruta != null ? ruta : "",
                    metodoHttp != null ? metodoHttp : "",
                    controlador != null ? controlador : "",
                    paquete != null ? paquete : "",
                    resultado != null ? resultado : "SUCCESS",
                    mensaje != null ? mensaje : "");

            // Escribir en archivo (append)
            Files.writeString(logFile, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException e) {
            // No lanzar excepción para no interrumpir el flujo normal
            System.err.println("Error escribiendo log de sesión: " + e.getMessage());
        }
    }

    /**
     * Borra el log de sesión de un usuario (llamado al re-login después de
     * reinicio)
     */
    public void limpiarLogSesionUsuario(Integer usuarioId) {
        if (usuarioId == null) {
            return;
        }

        try {
            Path logFile = Paths.get(SESIONES_DIR, "usuario_" + usuarioId + ".log");
            if (Files.exists(logFile)) {
                Files.delete(logFile);
            }
        } catch (IOException e) {
            System.err.println("Error limpiando log de sesión: " + e.getMessage());
        }
    }

    /**
     * Alias para logSesionToFile (usado por el interceptor)
     */
    @Async
    public void logAccionSesion(Integer usuarioId, String accion, String ruta,
            String metodoHttp, String controlador, String paquete,
            String resultado, String mensaje) {
        logSesionToFile(usuarioId, accion, ruta, metodoHttp, controlador, paquete, resultado, mensaje);
    }

    /**
     * Sobrecarga para logErrorToFile con detalles específicos (usado por
     * interceptor)
     */
    @Async
    public void logErrorToFile(String tipoError, String mensajeTecnico, String clase, String metodo, String paquete,
            String endpoint, Exception e) {
        try {
            // Crear directorio si no existe
            Path erroresPath = Paths.get(ERRORES_DIR);
            if (!Files.exists(erroresPath)) {
                Files.createDirectories(erroresPath);
            }

            // Archivo de log del día
            String fecha = LocalDateTime.now().format(DATE_FORMAT);
            Path logFile = erroresPath.resolve("error_" + fecha + ".log");

            String stackTrace = obtenerStackTrace(e);

            // Formatear línea de log
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logLine = String.format("[%s] | %s | %s | %s.%s | %s | %s | %s%n",
                    timestamp,
                    tipoError,
                    mensajeTecnico,
                    clase,
                    metodo,
                    paquete,
                    endpoint != null ? endpoint : "",
                    stackTrace.replace("\n", " | "));

            // Escribir en archivo (append)
            Files.writeString(logFile, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException ex) {
            System.err.println("Error escribiendo log de error: " + ex.getMessage());
        }
    }

    /**
     * Registra un error en archivo
     * Formato: [TIMESTAMP] | TIPO | MENSAJE_TECNICO | CLASE.METODO | PAQUETE |
     * ENDPOINT | STACK_TRACE
     */
    @Async
    public void logErrorToFile(Exception e, Integer usuarioId, String endpoint, String metodoHttp) {
        try {
            // Crear directorio si no existe
            Path erroresPath = Paths.get(ERRORES_DIR);
            if (!Files.exists(erroresPath)) {
                Files.createDirectories(erroresPath);
            }

            // Archivo de log del día
            String fecha = LocalDateTime.now().format(DATE_FORMAT);
            Path logFile = erroresPath.resolve("error_" + fecha + ".log");

            // Extraer información del error
            String tipoError = clasificarError(e);
            String mensajeTecnico = generarMensajeTecnico(e);
            String clase = "";
            String metodo = "";
            String paquete = "";

            if (e.getStackTrace().length > 0) {
                StackTraceElement elemento = e.getStackTrace()[0];
                clase = elemento.getClassName();
                metodo = elemento.getMethodName();
                paquete = extraerPaquete(clase);
            }

            String stackTrace = obtenerStackTrace(e);

            // Formatear línea de log
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logLine = String.format("[%s] | %s | %s | %s.%s | %s | %s | %s%n",
                    timestamp,
                    tipoError,
                    mensajeTecnico,
                    clase,
                    metodo,
                    paquete,
                    endpoint != null ? endpoint : "",
                    stackTrace.replace("\n", " | "));

            // Escribir en archivo (append)
            Files.writeString(logFile, logLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (IOException ex) {
            System.err.println("Error escribiendo log de error: " + ex.getMessage());
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

        // Mensajes específicos según tipo de excepción
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
    public String generarMensajeTecnico(Exception e) {
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
     * Limpia archivos de error antiguos (>30 días)
     */
    public void limpiarArchivosErrorAntiguos() {
        try {
            Path erroresPath = Paths.get(ERRORES_DIR);
            if (Files.exists(erroresPath)) {
                Files.list(erroresPath)
                        .filter(path -> path.toString().endsWith(".log"))
                        .filter(path -> {
                            try {
                                return Files.getLastModifiedTime(path).toMillis() < System.currentTimeMillis()
                                        - (30L * 24 * 60 * 60 * 1000);
                            } catch (IOException e) {
                                return false;
                            }
                        })
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Ignorar errores al eliminar
                            }
                        });
            }
        } catch (Exception e) {
            System.err.println("Error limpiando logs antiguos: " + e.getMessage());
        }
    }
}
