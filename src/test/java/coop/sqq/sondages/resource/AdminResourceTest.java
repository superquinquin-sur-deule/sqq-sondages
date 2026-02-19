package coop.sqq.sondages.resource;

import coop.sqq.sondages.entity.ServiceShift;
import coop.sqq.sondages.entity.SurveyResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class AdminResourceTest {

    @BeforeEach
    @Transactional
    void cleanDb() {
        ServiceShift.deleteAll();
        SurveyResponse.deleteAll();
    }

    @Test
    void dashboard_withoutAuth_redirectsToLogin() {
        given()
            .redirects().follow(false)
        .when()
            .get("/admin")
        .then()
            .statusCode(303)
            .header("Location", containsString("/admin/login"));
    }

    @Test
    void loginPage_returns200() {
        given()
            .when().get("/admin/login")
            .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("password"));
    }

    @Test
    void login_withWrongPassword_redirectsWithError() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("password", "wrong")
            .redirects().follow(false)
        .when()
            .post("/admin/login")
        .then()
            .statusCode(303)
            .header("Location", containsString("error"));
    }

    @Test
    void login_withCorrectPassword_redirectsToDashboardWithCookie() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("password", "sqq2025")
            .redirects().follow(false)
        .when()
            .post("/admin/login")
        .then()
            .statusCode(303)
            .header("Location", containsString("/admin"))
            .cookie("sqq_admin");
    }

    @Test
    void dashboard_withValidSession_returnsStatistics() {
        String token = loginAndGetToken();

        given()
            .cookie("sqq_admin", token)
        .when()
            .get("/admin")
        .then()
            .statusCode(200)
            .contentType(ContentType.HTML)
            .body(containsString("0")); // totalResponses = 0 with empty DB
    }

    @Test
    void dashboard_afterSubmission_showsUpdatedCount() {
        // Submit a survey response first
        given()
            .contentType(ContentType.URLENC)
            .formParam("LUN_MATIN", "on")
            .formParam("priority_1", "LUN_0")
            .formParam("priority_2", "MAR_1")
            .formParam("priority_3", "MER_2")
            .redirects().follow(false)
        .when()
            .post("/submit");

        String token = loginAndGetToken();

        given()
            .cookie("sqq_admin", token)
        .when()
            .get("/admin")
        .then()
            .statusCode(200)
            .body(containsString("1")); // totalResponses = 1
    }

    @Test
    void logout_clearsSessionAndRedirects() {
        String token = loginAndGetToken();

        // Logout
        given()
            .cookie("sqq_admin", token)
            .redirects().follow(false)
        .when()
            .get("/admin/logout")
        .then()
            .statusCode(303)
            .header("Location", containsString("/admin/login"));

        // Verify session is invalidated
        given()
            .cookie("sqq_admin", token)
            .redirects().follow(false)
        .when()
            .get("/admin")
        .then()
            .statusCode(303)
            .header("Location", containsString("/admin/login"));
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
