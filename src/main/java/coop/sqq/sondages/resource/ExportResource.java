package coop.sqq.sondages.resource;

import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.SurveyResponse;
import io.quarkus.logging.Log;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

            // Ligne d'en-tête : date + une colonne par semaine.
            Row header = sheet.createRow(0);
            int col = 0;
            createCell(header, col++, "Date réponse", headerStyle);
            for (SurveyConstants.Week week : SurveyConstants.WEEKS) {
                createCell(header, col++, week.label(), headerStyle);
            }

            // Lignes de données : "X" si la personne est absente cette semaine.
            int rowNum = 1;
            for (SurveyResponse r : responses) {
                Row row = sheet.createRow(rowNum++);
                col = 0;
                row.createCell(col++).setCellValue(r.submittedAt.format(FMT));

                String away = r.awayWeeks != null ? r.awayWeeks : "";
                Set<String> set = away.isBlank()
                        ? Set.of()
                        : new HashSet<>(Arrays.asList(away.split(",")));
                for (SurveyConstants.Week week : SurveyConstants.WEEKS) {
                    row.createCell(col++).setCellValue(set.contains(week.key()) ? "X" : "");
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
