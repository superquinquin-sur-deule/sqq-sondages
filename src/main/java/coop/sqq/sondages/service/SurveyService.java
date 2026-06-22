package coop.sqq.sondages.service;

import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.SurveyResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@ApplicationScoped
public class SurveyService {

    @Transactional
    public void submitSurvey(MultivaluedMap<String, String> formData) {
        SurveyResponse response = new SurveyResponse();
        response.submittedAt = LocalDateTime.now();

        // Collecte les semaines d'absence cochées, dans l'ordre canonique des semaines.
        StringJoiner joiner = new StringJoiner(",");
        for (SurveyConstants.Week week : SurveyConstants.WEEKS) {
            if (formData.containsKey(week.key())) {
                joiner.add(week.key());
            }
        }
        response.awayWeeks = joiner.toString();

        response.persist();
    }
}
