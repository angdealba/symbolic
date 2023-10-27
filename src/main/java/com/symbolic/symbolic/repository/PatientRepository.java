package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Patient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for Patient data.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
  List<Patient> findPatientsByPractitionersId(Long practitionerId);

  List<Patient> findPatientsByFacilitiesId(Long facilityId);

  List<Patient> findPatientsByInsurancePolicyId(Long policyId);

  Patient findPatientByAppointmentsId(Long appointmentId);

  Patient findPatientByPrescriptionsId(Long prescriptionId);

  Patient findPatientByDiagnosesId(Long diagnosisId);
}
