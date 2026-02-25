package coop.sqq.sondages.dto;

import java.util.List;
import java.util.Map;

public record StatisticsDto(
        long totalResponses,
        /** Q1: shopping slots - map of "DAY_SLOT" -> count */
        Map<String, Long> shoppingCounts,
        /** Q2: service shifts - map of "DAY_SLOTINDEX" -> weighted score */
        Map<String, Double> serviceScores,
        /** Q2 bis: service shifts - map of "DAY|SLOT" -> number of available people */
        Map<String, Long> serviceAvailability,
        List<String> days,
        List<String> shoppingSlots,
        List<String> serviceSlots,
        List<String> dayKeys,
        List<String> shoppingSlotKeys
) {
    /** Get shopping count by day index and slot index */
    public long shoppingCount(int dayIdx, int slotIdx) {
        String key = dayKeys.get(dayIdx) + "_" + shoppingSlotKeys.get(slotIdx);
        return shoppingCounts.getOrDefault(key, 0L);
    }

    /** Get shopping percentage by day index and slot index */
    public long shoppingPercent(int dayIdx, int slotIdx) {
        long count = shoppingCount(dayIdx, slotIdx);
        return totalResponses > 0 ? count * 100 / totalResponses : 0;
    }

    /** Get service score by day index and slot index */
    public int serviceScore(int dayIdx, int slotIdx) {
        String key = days.get(dayIdx) + "|" + SurveyConstants.serviceSlotsForDay(dayIdx).get(slotIdx);
        Double score = serviceScores.getOrDefault(key, 0.0);
        return score.intValue();
    }

    /** Get service availability count by day index and slot index */
    public long serviceAvailability(int dayIdx, int slotIdx) {
        String key = days.get(dayIdx) + "|" + SurveyConstants.serviceSlotsForDay(dayIdx).get(slotIdx);
        return serviceAvailability.getOrDefault(key, 0L);
    }

    /** Get service slot label for a given day and slot index (samedi differs) */
    public String serviceSlotLabel(int dayIdx, int slotIdx) {
        return SurveyConstants.serviceSlotsForDay(dayIdx).get(slotIdx);
    }
}
