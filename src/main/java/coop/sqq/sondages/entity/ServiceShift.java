package coop.sqq.sondages.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "service_shift")
public class ServiceShift extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    public SurveyResponse surveyResponse;

    @Column(nullable = false)
    public String day;

    @Column(name = "time_slot", nullable = false)
    public String timeSlot;

    @Column(nullable = false)
    public int priority;
}
