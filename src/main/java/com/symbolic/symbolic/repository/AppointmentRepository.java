package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAppointmentsByPatientId(Long patientId);
    List<Appointment> findAppointmentsByPractitionerId(Long practitionerId);
    List<Appointment> findAppointmentsByFacilityId(Long facilityId);
}
