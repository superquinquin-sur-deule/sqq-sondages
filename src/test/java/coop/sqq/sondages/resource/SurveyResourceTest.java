package coop.sqq.sondages.resource;

import coop.sqq.sondages.entity.ServiceShift;
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
        ServiceShift.deleteAll();
        SurveyResponse.deleteAll();
    }

    @Test
    void showSurvey_returnsFormWithDaysAndSlots() {
        given()
            .when().get("/")
            .then()
                .statusCode(200)
                .contentType(ContentType.HTML)
                .body(containsString("Lundi"))
                .body(containsString("Samedi"))
                .body(containsString("Matin"))
                .body(containsString("LUN_MATIN"));
    }

    @Test
    void submitSurvey_redirectsToThankYouPage() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("LUN_MATIN", "on")
            .formParam("MAR_MIDI", "on")
            .formParam("priority_1", "LUN_0")
            .formParam("priority_2", "MAR_1")
            .formParam("priority_3", "MER_2")
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
            .formParam("LUN_MATIN", "on")
            .formParam("VEN_SOIR", "on")
            .formParam("priority_1", "LUN_0")
            .formParam("priority_2", "SAM_4")
            .formParam("priority_3", "JEU_3")
            .redirects().follow(false)
        .when()
            .post("/submit");

        List<SurveyResponse> responses = SurveyResponse.listAll();
        assertEquals(1, responses.size());

        SurveyResponse response = responses.getFirst();
        assertEquals("LUN_MATIN,VEN_SOIR", response.shoppingSlots);

        List<ServiceShift> shifts = ServiceShift.list("surveyResponse", response);
        assertEquals(3, shifts.size());
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
    void submitSurvey_withOnlyShoppingSlots_persistsWithoutShifts() {
        given()
            .contentType(ContentType.URLENC)
            .formParam("LUN_MATIN", "on")
            .redirects().follow(false)
        .when()
            .post("/submit");
    }
}
