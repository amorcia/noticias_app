package com.noticias.web.servicios;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.noticias.web.dtos.DenunciaDTO;
import com.noticias.web.dtos.NoticiaEliminadaDTO;
import com.noticias.web.dtos.UsuarioDTO;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la generación de reportes en PDF usando iText 7.
 */
@Service
public class ExportacionServicio {

    private final ApiNoticiasCliente apiCliente;

    public ExportacionServicio(ApiNoticiasCliente apiCliente) {
        this.apiCliente = apiCliente;
    }

    public byte[] generarReporteCompleto() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(20, 20, 20, 20);

            // Header con diseño "Logo"
            Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1 }));
            headerTable.setWidth(UnitValue.createPercentValue(100));
            Cell headerCell = new Cell()
                    .add(new Paragraph("NoticiasApp - Reporte de Gestión")
                            .setFontSize(22)
                            .setBold()
                            .setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(ColorConstants.DARK_GRAY)
                    .setPadding(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
            headerTable.addCell(headerCell);
            document.add(headerTable);

            // Línea de acento azul
            Table lineTable = new Table(UnitValue.createPercentArray(new float[] { 1 }));
            lineTable.setWidth(UnitValue.createPercentValue(100));
            lineTable.addCell(new Cell().setHeight(5).setBackgroundColor(ColorConstants.BLUE).setBorder(null));
            document.add(lineTable);

            document.add(new Paragraph(
                    "Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(5)
                    .setMarginBottom(15));

            // Estadísticas Generales
            agregarSeccionEstadisticas(document);

            // Usuarios Vetados
            agregarSeccionUsuariosVetados(document);

            // Noticias Eliminadas
            agregarSeccionNoticiasEliminadas(document);

            // Denuncias Pendientes
            agregarSeccionDenuncias(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void agregarSeccionEstadisticas(Document document) {
        document.add(new Paragraph("RESUMEN DE ESTADÍSTICAS")
                .setFontSize(14)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginTop(15));

        try {
            Map<String, Object> stats = apiCliente.getAdminStats();
            if (stats != null) {
                Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 1 }));
                table.setWidth(UnitValue.createPercentValue(100));

                stats.forEach((k, v) -> {
                    table.addCell(new Cell().add(new Paragraph(convertKeyToFriendly(k)).setBold())
                            .setBackgroundColor(ColorConstants.LIGHT_GRAY));
                    table.addCell(new Cell().add(new Paragraph(String.valueOf(v))));
                });

                document.add(table);
            } else {
                document.add(new Paragraph("No hay estadísticas disponibles."));
            }
        } catch (Exception e) {
            document.add(
                    new Paragraph("Error al cargar estadísticas: " + e.getMessage()).setFontColor(ColorConstants.RED));
        }
    }

    private String convertKeyToFriendly(String key) {
        switch (key) {
            case "totalUsuarios":
                return "Total de Usuarios";
            case "usuariosVetados":
                return "Usuarios Vetados";
            case "totalNoticias":
                return "Total de Noticias";
            case "totalSanciones":
                return "Total Sanciones";
            case "sancionesPendientes":
                return "Sanciones Pendientes";
            case "porcentajeVetados":
                return "% Usuarios Vetados";
            default:
                return key;
        }
    }

    private void agregarSeccionUsuariosVetados(Document document) {
        document.add(new Paragraph("USUARIOS VETADOS")
                .setFontSize(14)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginTop(20));

        try {
            List<UsuarioDTO> vetados = apiCliente.listarVetados();
            if (vetados != null && !vetados.isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 3, 2, 2 }));
                table.setWidth(UnitValue.createPercentValue(100));

                String[] headers = { "Usuario", "Motivo", "Fecha Veto", "Duración" };
                for (String h : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(ColorConstants.BLUE));
                }

                int i = 0;
                for (UsuarioDTO u : vetados) {
                    com.itextpdf.kernel.colors.Color bg = (i++ % 2 == 0) ? ColorConstants.WHITE
                            : ColorConstants.LIGHT_GRAY;
                    table.addCell(new Cell().add(new Paragraph(u.getNombreCompleto() + "\n" + u.getEmail()))
                            .setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(u.getMotivoVeto() != null ? u.getMotivoVeto() : "-"))
                            .setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(
                            u.getFechaVeto() != null ? u.getFechaVeto().toString().substring(0, 10) : "-"))
                            .setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(
                            u.getVetadoHasta() != null ? u.getVetadoHasta().toString().substring(0, 10) : "Permanente"))
                            .setBackgroundColor(bg));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No hay usuarios vetados actualmente."));
            }
        } catch (Exception e) {
            document.add(new Paragraph("Error al cargar usuarios vetados.").setFontColor(ColorConstants.RED));
        }
    }

    private void agregarSeccionNoticiasEliminadas(Document document) {
        document.add(new Paragraph("NOTICIAS ELIMINADAS (HISTORIAL)")
                .setFontSize(14)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginTop(20));

        try {
            List<NoticiaEliminadaDTO> eliminadas = apiCliente.listarNoticiasEliminadas();
            if (eliminadas != null && !eliminadas.isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[] { 3, 3, 3, 2 }));
                table.setWidth(UnitValue.createPercentValue(100));

                String[] headers = { "Título", "Motivo / Descripción", "Eliminado Por", "Fecha" };
                for (String h : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(ColorConstants.BLUE));
                }

                int i = 0;
                for (NoticiaEliminadaDTO n : eliminadas) {
                    com.itextpdf.kernel.colors.Color bg = (i++ % 2 == 0) ? ColorConstants.WHITE
                            : ColorConstants.LIGHT_GRAY;
                    table.addCell(new Cell().add(new Paragraph(n.getTitulo()).setBold()).setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph("Motivo: " + n.getMotivo() + "\nDesc: "
                            + (n.getDescripcion() != null ? n.getDescripcion() : "-"))).setBackgroundColor(bg));
                    table.addCell(new Cell()
                            .add(new Paragraph(n.getEliminadoPorNombre() + "\n(" + n.getRolEliminador() + ")"))
                            .setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(n.getFechaEliminacion().toString().substring(0, 10)))
                            .setBackgroundColor(bg));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No hay noticias eliminadas registradas."));
            }
        } catch (Exception e) {
            document.add(new Paragraph("Error al cargar noticias eliminadas.").setFontColor(ColorConstants.RED));
        }
    }

    private void agregarSeccionDenuncias(Document document) {
        document.add(new Paragraph("DENUNCIAS RECIENTES")
                .setFontSize(14)
                .setBold()
                .setFontColor(ColorConstants.BLUE)
                .setMarginTop(20));

        try {
            List<DenunciaDTO> denuncias = apiCliente.listarDenuncias();
            if (denuncias != null && !denuncias.isEmpty()) {
                Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 2, 4, 1 }));
                table.setWidth(UnitValue.createPercentValue(100));

                String[] headers = { "Denunciante", "Acusado", "Motivo", "Estado" };
                for (String h : headers) {
                    table.addHeaderCell(new Cell().add(new Paragraph(h).setBold().setFontColor(ColorConstants.WHITE))
                            .setBackgroundColor(ColorConstants.BLUE));
                }

                int i = 0;
                for (DenunciaDTO d : denuncias) {
                    com.itextpdf.kernel.colors.Color bg = (i++ % 2 == 0) ? ColorConstants.WHITE
                            : ColorConstants.LIGHT_GRAY;
                    String denunciante = d.getDenunciante() != null ? d.getDenunciante().getNombreCompleto()
                            : "Anónimo";
                    String acusado = "Desconocido";
                    if (d.getNoticia() != null) {
                        acusado = "Noticia: " + d.getNoticia().getTitulo();
                    }

                    table.addCell(new Cell().add(new Paragraph(denunciante)).setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(acusado)).setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(d.getMotivo() != null ? d.getMotivo() : "-"))
                            .setBackgroundColor(bg));
                    table.addCell(new Cell().add(new Paragraph(d.getEstado() != null ? d.getEstado() : "-"))
                            .setBackgroundColor(bg));
                }
                document.add(table);
            } else {
                document.add(new Paragraph("No hay denuncias recientes."));
            }
        } catch (Exception e) {
            document.add(new Paragraph("Error al cargar denuncias.").setFontColor(ColorConstants.RED));
        }
    }
}
