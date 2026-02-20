package coop.sqq.sondages.resource;

import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.ServiceShift;
import coop.sqq.sondages.entity.SurveyResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Path("/admin/export")
public class ExportResource {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @GET
    @Produces("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public Response exportXlsx(@CookieParam("sqq_admin") String token) throws IOException {
        Log.info("Exporting survey responses");
        if (!AdminResource.isAuthenticated(token)) {
            return Response.seeOther(java.net.URI.create("/admin/login")).build();
        }

        List<SurveyResponse> responses = SurveyResponse.listAll();

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            CellStyle headerStyle = createHeaderStyle(workbook);
            Sheet sheet = workbook.createSheet("Réponses");

            // Header row
            Row header = sheet.createRow(0);
            int col = 0;
            createCell(header, col++, "Date réponse", headerStyle);
            for (String day : SurveyConstants.DAYS) {
                for (String slot : SurveyConstants.SHOPPING_SLOTS) {
                    createCell(header, col++, day + " - " + slot, headerStyle);
                }
            }
            for (int p = 1; p <= 3; p++) {
                createCell(header, col++, "Permanence choix " + p, headerStyle);
            }

            // Data rows
            int rowNum = 1;
            for (SurveyResponse r : responses) {
                Row row = sheet.createRow(rowNum++);
                col = 0;
                row.createCell(col++).setCellValue(r.submittedAt.format(FMT));

                // Q1: shopping slots
                String slots = r.shoppingSlots != null ? r.shoppingSlots : "";
                for (int d = 0; d < SurveyConstants.DAYS.size(); d++) {
                    for (int s = 0; s < SurveyConstants.SHOPPING_SLOTS.size(); s++) {
                        String key = SurveyConstants.shoppingKey(d, s);
                        row.createCell(col++).setCellValue(slots.contains(key) ? "X" : "");
                    }
                }

                // Q2: service shifts (priority 1, 2, 3)
                List<ServiceShift> shifts = ServiceShift.list("surveyResponse", r);
                shifts.sort(Comparator.comparingInt(s -> s.priority));
                for (int p = 1; p <= 3; p++) {
                    final int priority = p;
                    String label = shifts.stream()
                            .filter(s -> s.priority == priority)
                            .findFirst()
                            .map(s -> s.day + " | " + s.timeSlot)
                            .orElse("");
                    row.createCell(col++).setCellValue(label);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return Response.ok(out.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    .header("Content-Disposition", "attachment; filename=\"sondage-sqq.xlsx\"")
                    .build();
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GOLD.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
