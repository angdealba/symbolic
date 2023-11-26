package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Appointment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Provides a set of search methods from Spring Boot for Appointment data.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  List<Appointment> findAppointmentsByPatientId(UUID patientId);

  List<Appointment> findAppointmentsByPractitionerId(UUID practitionerId);

  List<Appointment> findAppointmentsByFacilityId(UUID facilityId);
}
