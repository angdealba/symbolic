package com.symbolic.symbolic.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * A data model for an appointment, representing its time and cost.
 */
@Entity
@Table(name = "appointments")
@NoArgsConstructor
public class Appointment {
    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "dateTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTime;

    @Column(name = "cost")
    private Integer cost;

    /**
     * A constructor for the Appointment data model.
     * @param dateTime a Date value representing when the appointment will take place
     * @param cost an integer value representing the cost of the appointment
     */
    public Appointment(Date dateTime, Integer cost) {
        this.dateTime = dateTime;
        this.cost = cost;
    }

    public Long getId() {
        return id;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Integer getCost() {
        return cost;
    }

    public void setCost(Integer cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return Objects.equals(id, that.id) && Objects.equals(dateTime, that.dateTime) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, dateTime, cost);
    }
}
