package coop.sqq.sondages.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class SurveyConstants {

    private SurveyConstants() {}

    /** Une case du calendrier (un jour d'une semaine). */
    public record Day(int dayOfMonth, boolean inMonth) {}

    /** Une semaine sélectionnable (lundi → dimanche). */
    public record Week(String key, String label, List<Day> days) {}

    /** Un mois du calendrier avec ses semaines. */
    public record MonthSection(String monthLabel, List<Week> weeks) {}

    /** Spécification compacte d'une semaine : clé, label, lundi, mois de classement, libellé du mois. */
    private record WeekSpec(String key, String label, LocalDate monday, int filingMonth, String monthLabel) {}

    /**
     * Semaines de l'été 2026 (juillet + août). Pour changer la période couverte,
     * éditer UNIQUEMENT cette liste. Les clés (numéros de semaine ISO) ne doivent jamais
     * être réutilisées pour une autre semaine une fois des données en prod.
     */
    private static final List<WeekSpec> SPECS = List.of(
            new WeekSpec("S27", "29 juin – 5 juil.", LocalDate.of(2026, 6, 29), 7, "Juillet 2026"),
            new WeekSpec("S28", "6 – 12 juil.",      LocalDate.of(2026, 7, 6),  7, "Juillet 2026"),
            new WeekSpec("S29", "13 – 19 juil.",     LocalDate.of(2026, 7, 13), 7, "Juillet 2026"),
            new WeekSpec("S30", "20 – 26 juil.",     LocalDate.of(2026, 7, 20), 7, "Juillet 2026"),
            new WeekSpec("S31", "27 juil. – 2 août", LocalDate.of(2026, 7, 27), 7, "Juillet 2026"),
            new WeekSpec("S32", "3 – 9 août",        LocalDate.of(2026, 8, 3),  8, "Août 2026"),
            new WeekSpec("S33", "10 – 16 août",      LocalDate.of(2026, 8, 10), 8, "Août 2026"),
            new WeekSpec("S34", "17 – 23 août",      LocalDate.of(2026, 8, 17), 8, "Août 2026"),
            new WeekSpec("S35", "24 – 30 août",      LocalDate.of(2026, 8, 24), 8, "Août 2026")
    );

    /** Toutes les semaines, ordre chronologique (parsing, stats, export). */
    public static final List<Week> WEEKS;

    /** Calendrier groupé par mois (formulaire). */
    public static final List<MonthSection> CALENDAR;

    static {
        List<Week> weeks = new ArrayList<>();
        for (WeekSpec spec : SPECS) {
            List<Day> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                LocalDate date = spec.monday().plusDays(i);
                days.add(new Day(date.getDayOfMonth(), date.getMonthValue() == spec.filingMonth()));
            }
            weeks.add(new Week(spec.key(), spec.label(), List.copyOf(days)));
        }
        WEEKS = List.copyOf(weeks);

        List<MonthSection> sections = new ArrayList<>();
        String currentLabel = null;
        List<Week> current = null;
        for (int idx = 0; idx < SPECS.size(); idx++) {
            WeekSpec spec = SPECS.get(idx);
            if (!spec.monthLabel().equals(currentLabel)) {
                if (current != null) {
                    sections.add(new MonthSection(currentLabel, List.copyOf(current)));
                }
                currentLabel = spec.monthLabel();
                current = new ArrayList<>();
            }
            current.add(WEEKS.get(idx));
        }
        if (current != null) {
            sections.add(new MonthSection(currentLabel, List.copyOf(current)));
        }
        CALENDAR = List.copyOf(sections);
    }

    /** Clés de semaine valides (validation / parsing). */
    public static Set<String> weekKeys() {
        return WEEKS.stream().map(Week::key).collect(Collectors.toUnmodifiableSet());
    }
}
