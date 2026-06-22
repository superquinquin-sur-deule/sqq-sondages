package coop.sqq.sondages.dto;

import java.util.List;
import java.util.Map;

public record StatisticsDto(
        long totalResponses,
        /** Clé de semaine -> nombre de personnes absentes cette semaine. */
        Map<String, Long> awayCounts,
        List<SurveyConstants.Week> weeks
) {
    /** Nombre de personnes absentes pour une semaine donnée. */
    public long awayCount(String key) {
        return awayCounts.getOrDefault(key, 0L);
    }

    /** Pourcentage de personnes absentes pour une semaine donnée. */
    public long awayPercent(String key) {
        long count = awayCount(key);
        return totalResponses > 0 ? count * 100 / totalResponses : 0;
    }
}
