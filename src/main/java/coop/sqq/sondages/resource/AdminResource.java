package coop.sqq.sondages.resource;

import coop.sqq.sondages.dto.StatisticsDto;
import coop.sqq.sondages.service.StatisticsService;
import io.quarkus.logging.Log;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Path("/admin")
public class AdminResource {

    private static final Map<String, Boolean> sessions = new ConcurrentHashMap<>();

    @ConfigProperty(name = "sqq.admin.password")
    String adminPassword;

    @Inject
    @Location("AdminResource/login.html")
    Template login;

    @Inject
    @Location("AdminResource/dashboard.html")
    Template dashboard;

    @Inject
    StatisticsService statisticsService;

    @GET
    @Path("/login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showLogin(@QueryParam("error") String error) {
        return login.data("error", error != null);
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response doLogin(@FormParam("password") String password) {
        if (adminPassword.equals(password)) {
            Log.info("Admin logged in");
            String token = UUID.randomUUID().toString();
            sessions.put(token, true);
            NewCookie cookie = new NewCookie.Builder("sqq_admin")
                    .value(token)
                    .path("/admin")
                    .httpOnly(true)
                    .build();
            return Response.seeOther(URI.create("/admin")).cookie(cookie).build();
        }
        Log.warn("Admin login failed");
        return Response.seeOther(URI.create("/admin/login?error=1")).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response showDashboard(@CookieParam("sqq_admin") String token) {
        if (!isAuthenticated(token)) {
            return Response.seeOther(URI.create("/admin/login")).build();
        }
        StatisticsDto stats = statisticsService.computeStatistics();
        TemplateInstance page = dashboard.data("stats", stats);
        return Response.ok(page).build();
    }

    @GET
    @Path("/logout")
    public Response logout(@CookieParam("sqq_admin") String token) {
        if (token != null) {
            sessions.remove(token);
        }
        NewCookie cookie = new NewCookie.Builder("sqq_admin")
                .value("")
                .path("/admin")
                .maxAge(0)
                .httpOnly(true)
                .build();
        return Response.seeOther(URI.create("/admin/login")).cookie(cookie).build();
    }

    public static boolean isAuthenticated(String token) {
        return token != null && sessions.containsKey(token);
    }
}
