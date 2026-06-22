package coop.sqq.sondages.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "survey_response")
public class SurveyResponse extends PanacheEntity {

    @Column(name = "submitted_at", nullable = false)
    public LocalDateTime submittedAt;

    /** CSV des clés de semaines d'absence, ex. "S28,S29,S31" ; "" = présent tout l'été. */
    @Column(name = "away_weeks", columnDefinition = "TEXT")
    public String awayWeeks;
}
