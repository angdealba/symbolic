package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Appointment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Provides a set of search methods from Spring Boot for Appointment data.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
  List<Appointment> findAppointmentsByPatientId(Long patientId);

  List<Appointment> findAppointmentsByPractitionerId(Long practitionerId);

  List<Appointment> findAppointmentsByFacilityId(Long facilityId);
}
