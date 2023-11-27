package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Patient;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for Patient data.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {
  List<Patient> findPatientsByPractitionersId(UUID practitionerId);

  List<Patient> findPatientsByFacilitiesId(UUID facilityId);

  List<Patient> findPatientsByInsurancePolicyId(UUID policyId);

  Patient findPatientByAppointmentsId(UUID appointmentId);

  Patient findPatientByPrescriptionsId(UUID prescriptionId);

  Patient findPatientByDiagnosesId(UUID diagnosisId);
}
