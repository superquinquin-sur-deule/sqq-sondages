package coop.sqq.sondages.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SurveyConstants {

    private SurveyConstants() {}

    public static final List<String> DAYS = List.of(
            "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi");

    public static final List<String> SHOPPING_SLOTS = List.of(
            "Matin", "Midi", "Après-midi", "Soir (après 18h)");

    public static final List<String> SERVICE_SLOTS = List.of(
            "8h45 - 11h30", "11h15 - 14h00", "13h45 - 16h30",
            "16h15 - 19h00", "18h45 - 21h30");

    public static final List<String> SERVICE_SLOTS_SAMEDI = List.of(
            "8h45 - 11h30", "11h15 - 14h00", "13h45 - 16h30",
            "16h15 - 19h00", "18h45 - 20h45");

    public static final List<String> DAY_KEYS = List.of(
            "LUN", "MAR", "MER", "JEU", "VEN", "SAM");

    public static final List<String> SHOPPING_SLOT_KEYS = List.of(
            "MATIN", "MIDI", "APREM", "SOIR");

    /** Maps display label to key for days */
    public static final Map<String, String> DAY_LABEL_TO_KEY;

    static {
        DAY_LABEL_TO_KEY = new LinkedHashMap<>();
        for (int i = 0; i < DAYS.size(); i++) {
            DAY_LABEL_TO_KEY.put(DAYS.get(i), DAY_KEYS.get(i));
        }
    }

    /** Build a form key like "LUN_MATIN" */
    public static String shoppingKey(int dayIndex, int slotIndex) {
        return DAY_KEYS.get(dayIndex) + "_" + SHOPPING_SLOT_KEYS.get(slotIndex);
    }

    /** Get the service slots list for a given day index (samedi has different last slot) */
    public static List<String> serviceSlotsForDay(int dayIndex) {
        return dayIndex == 5 ? SERVICE_SLOTS_SAMEDI : SERVICE_SLOTS;
    }

    /** Build a service shift value like "Lundi | 8h45 - 11h30" */
    public static String serviceShiftLabel(int dayIndex, int slotIndex) {
        return DAYS.get(dayIndex) + " | " + serviceSlotsForDay(dayIndex).get(slotIndex);
    }

    /** Build a service shift value like "LUN_0" */
    public static String serviceShiftKey(int dayIndex, int slotIndex) {
        return DAY_KEYS.get(dayIndex) + "_" + slotIndex;
    }

    /** All 30 service shift options as key -> label pairs */
    public static Map<String, String> allServiceShiftOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        for (int d = 0; d < DAYS.size(); d++) {
            for (int s = 0; s < SERVICE_SLOTS.size(); s++) {
                options.put(serviceShiftKey(d, s), serviceShiftLabel(d, s));
            }
        }
        return options;
    }
}
