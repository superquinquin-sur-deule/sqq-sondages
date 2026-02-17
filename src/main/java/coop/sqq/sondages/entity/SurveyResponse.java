package coop.sqq.sondages.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "survey_response")
public class SurveyResponse extends PanacheEntity {

    @Column(name = "submitted_at", nullable = false)
    public LocalDateTime submittedAt;

    @Column(name = "shopping_slots", columnDefinition = "TEXT")
    public String shoppingSlots;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ServiceShift> serviceShifts;
}
