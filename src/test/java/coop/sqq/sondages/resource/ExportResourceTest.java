package coop.sqq.sondages.resource;

import coop.sqq.sondages.entity.SurveyResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class ExportResourceTest {

    @BeforeEach
    @Transactional
    void cleanDb() {
        SurveyResponse.deleteAll();
    }

    @Test
    void export_withoutAuth_redirectsToLogin() {
        given()
            .redirects().follow(false)
        .when()
            .get("/admin/export")
        .then()
            .statusCode(303)
            .header("Location", containsString("/admin/login"));
    }

    @Test
    void export_withAuth_returnsXlsx() {
        String token = loginAndGetToken();

        given()
            .cookie("sqq_admin", token)
        .when()
            .get("/admin/export")
        .then()
            .statusCode(200)
            .contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Content-Disposition", containsString("sondage-sqq.xlsx"));
    }

    @Test
    void export_withData_containsSubmittedResponse() throws IOException {
        // Submit a survey response (away week S27 = first week column)
        given()
            .contentType(ContentType.URLENC)
            .formParam("S27", "on")
            .redirects().follow(false)
        .when()
            .post("/submit");

        String token = loginAndGetToken();

        byte[] xlsxBytes = given()
            .cookie("sqq_admin", token)
        .when()
            .get("/admin/export")
        .then()
            .statusCode(200)
            .extract().asByteArray();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(xlsxBytes))) {
            XSSFSheet sheet = workbook.getSheet("Réponses");
            assertNotNull(sheet);
            // Header row + 1 data row
            assertEquals(2, sheet.getPhysicalNumberOfRows());
            // First header cell
            assertEquals("Date réponse", sheet.getRow(0).getCell(0).getStringCellValue());
            // Week S27 is the first week column (column 1) and should be "X"
            assertEquals("X", sheet.getRow(1).getCell(1).getStringCellValue());
        }
    }

    private String loginAndGetToken() {
        Response response = given()
            .contentType(ContentType.URLENC)
            .formParam("password", "sqq2025")
            .redirects().follow(false)
        .when()
            .post("/admin/login");

        return response.cookie("sqq_admin");
    }
}
