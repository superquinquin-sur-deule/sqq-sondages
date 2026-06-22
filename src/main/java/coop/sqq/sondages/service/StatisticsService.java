package coop.sqq.sondages.service;

import coop.sqq.sondages.dto.StatisticsDto;
import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.SurveyResponse;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class StatisticsService {

    public StatisticsDto computeStatistics() {
        List<SurveyResponse> responses = SurveyResponse.listAll();
        long total = responses.size();

        // Amorce chaque semaine à 0 pour que les semaines sans absence s'affichent quand même.
        Map<String, Long> awayCounts = new LinkedHashMap<>();
        for (SurveyConstants.Week w : SurveyConstants.WEEKS) {
            awayCounts.put(w.key(), 0L);
        }
        for (SurveyResponse r : responses) {
            if (r.awayWeeks != null && !r.awayWeeks.isBlank()) {
                for (String key : r.awayWeeks.split(",")) {
                    String k = key.trim();
                    if (awayCounts.containsKey(k)) { // ignore les clés de semaines supprimées
                        awayCounts.merge(k, 1L, Long::sum);
                    }
                }
            }
        }

        return new StatisticsDto(total, awayCounts, SurveyConstants.WEEKS);
    }
}
