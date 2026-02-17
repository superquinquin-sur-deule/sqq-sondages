package coop.sqq.sondages.service;

import coop.sqq.sondages.dto.SurveyConstants;
import coop.sqq.sondages.entity.ServiceShift;
import coop.sqq.sondages.entity.SurveyResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.StringJoiner;

@ApplicationScoped
public class SurveyService {

    @Transactional
    public void submitSurvey(MultivaluedMap<String, String> formData) {
        SurveyResponse response = new SurveyResponse();
        response.submittedAt = LocalDateTime.now();
        response.serviceShifts = new ArrayList<>();

        // Q1: collect checked shopping slots
        StringJoiner slotsJoiner = new StringJoiner(",");
        for (int d = 0; d < SurveyConstants.DAYS.size(); d++) {
            for (int s = 0; s < SurveyConstants.SHOPPING_SLOTS.size(); s++) {
                String key = SurveyConstants.shoppingKey(d, s);
                if (formData.containsKey(key)) {
                    slotsJoiner.add(key);
                }
            }
        }
        response.shoppingSlots = slotsJoiner.toString();

        response.persist();

        // Q2: collect service shift priorities
        for (int priority = 1; priority <= 3; priority++) {
            String value = formData.getFirst("priority_" + priority);
            if (value != null && !value.isBlank()) {
                String[] parts = value.split("_", 2);
                if (parts.length == 2) {
                    String dayKey = parts[0];
                    int slotIndex = Integer.parseInt(parts[1]);

                    int dayIndex = SurveyConstants.DAY_KEYS.indexOf(dayKey);
                    if (dayIndex >= 0 && slotIndex >= 0 && slotIndex < SurveyConstants.serviceSlotsForDay(dayIndex).size()) {
                        ServiceShift shift = new ServiceShift();
                        shift.surveyResponse = response;
                        shift.day = SurveyConstants.DAYS.get(dayIndex);
                        shift.timeSlot = SurveyConstants.serviceSlotsForDay(dayIndex).get(slotIndex);
                        shift.priority = priority;
                        shift.persist();
                    }
                }
            }
        }
    }
}
