package coop.sqq.sondages.service;

import coop.sqq.sondages.dto.StatisticsDto;
import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.ServiceShift;
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

        // Q1: count shopping slots
        Map<String, Long> shoppingCounts = new LinkedHashMap<>();
        for (int d = 0; d < SurveyConstants.DAYS.size(); d++) {
            for (int s = 0; s < SurveyConstants.SHOPPING_SLOTS.size(); s++) {
                shoppingCounts.put(SurveyConstants.shoppingKey(d, s), 0L);
            }
        }
        for (SurveyResponse r : responses) {
            if (r.shoppingSlots != null && !r.shoppingSlots.isBlank()) {
                for (String slot : r.shoppingSlots.split(",")) {
                    shoppingCounts.merge(slot.trim(), 1L, Long::sum);
                }
            }
        }

        // Q2: weighted scores for service shifts
        Map<String, Double> serviceScores = new LinkedHashMap<>();
        for (int d = 0; d < SurveyConstants.DAYS.size(); d++) {
            List<String> slots = SurveyConstants.serviceSlotsForDay(d);
            for (int s = 0; s < slots.size(); s++) {
                serviceScores.put(SurveyConstants.DAYS.get(d) + "|" + slots.get(s), 0.0);
            }
        }

        List<ServiceShift> shifts = ServiceShift.listAll();
        for (ServiceShift shift : shifts) {
            String key = shift.day + "|" + shift.timeSlot;
            double weight = switch (shift.priority) {
                case 1 -> 3.0;
                case 2 -> 2.0;
                case 3 -> 1.0;
                default -> 0.0;
            };
            serviceScores.merge(key, weight, Double::sum);
        }

        return new StatisticsDto(
                total,
                shoppingCounts,
                serviceScores,
                SurveyConstants.DAYS,
                SurveyConstants.SHOPPING_SLOTS,
                SurveyConstants.SERVICE_SLOTS,
                SurveyConstants.DAY_KEYS,
                SurveyConstants.SHOPPING_SLOT_KEYS
        );
    }
}
