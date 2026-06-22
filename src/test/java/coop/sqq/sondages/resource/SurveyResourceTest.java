package coop.sqq.sondages.resource;

import coop.sqq.sondages.entity.SurveyResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class SurveyResourceTest {

    @BeforeEach
    @Transactional
    void cleanDb() {
        SurveyResponse.deleteAll();
    }

    @Test
    void showSurvey_returnsFormWithCalendar() {
        given()
            .when().get("/")
            .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("Tu pars en vacances"))
                .body(containsString("Juillet 2026"))
                .body(containsString("Août 2026"))
                .body(containsString("name=\"S28\""));
    }

    @Test
    void submitSurvey_redirectsToThankYouPage() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("S28", "on")
            .formParam("S29", "on")
            .redirects().follow(false)
        .when()
            .post("/submit")
        .then()
            .statusCode(303)
            .header("Location", containsString("/merci"));
    }

    @Test
    @Transactional
    void submitSurvey_persistsResponseInDatabase() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("S28", "on")
            .formParam("S29", "on")
            .formParam("S31", "on")
            .redirects().follow(false)
        .when()
            .post("/submit");

        List<SurveyResponse> responses = SurveyResponse.listAll();
        assertEquals(1, responses.size());

        SurveyResponse response = responses.getFirst();
        assertEquals("S28,S29,S31", response.awayWeeks);
    }

    @Test
    void showThankYou_returnsConfirmation() {
        given()
            .when().get("/merci")
            .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("Merci"));
    }

    @Test
    @Transactional
    void submitSurvey_withNoWeeks_persistsEmpty() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("ignored", "on")
            .redirects().follow(false)
        .when()
            .post("/submit");

        List<SurveyResponse> responses = SurveyResponse.listAll();
        assertEquals(1, responses.size());
        assertEquals("", responses.getFirst().awayWeeks);
    }
}
