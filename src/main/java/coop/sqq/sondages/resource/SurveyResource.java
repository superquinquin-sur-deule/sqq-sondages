package coop.sqq.sondages.resource;

import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.service.SurveyService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("/")
public class SurveyResource {

    @Inject
    @Location("SurveyResource/survey.html")
    Template survey;

    @Inject
    @Location("SurveyResource/thankyou.html")
    Template thankyou;

    @Inject
    SurveyService surveyService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showSurvey() {
        return survey.data("days", SurveyConstants.DAYS)
                .data("dayKeys", SurveyConstants.DAY_KEYS)
                .data("shoppingSlots", SurveyConstants.SHOPPING_SLOTS)
                .data("shoppingSlotKeys", SurveyConstants.SHOPPING_SLOT_KEYS)
                .data("serviceOptions", SurveyConstants.allServiceShiftOptions());
    }

    @POST
    @Path("/submit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submitSurvey(MultivaluedMap<String, String> formData) {
        surveyService.submitSurvey(formData);
        return Response.seeOther(URI.create("/merci")).build();
    }

    @GET
    @Path("/merci")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showThankYou() {
        return thankyou.instance();
    }
}
