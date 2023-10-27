package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for MedicalPractitioner data.
 */
@Repository
public interface MedicalPractitionerRepository extends JpaRepository<MedicalPractitioner, Long> {
  List<MedicalPractitioner> findMedicalPractitionerByPatientsId(Long patientId);

  List<MedicalPractitioner> findMedicalPractitionerByFacilityId(Long facilityId);

  MedicalPractitioner findMedicalPractitionerByAppointmentsId(Long appointmentId);

  MedicalPractitioner findMedicalPractitionerByPrescriptionsId(Long prescriptionId);

  MedicalPractitioner findMedicalPractitionerByDiagnosesId(Long diagnosisId);

  List<MedicalPractitioner> findByLatitudeBetweenAndLongitudeBetween(Double minLatitude,
                                                                     Double maxLatitude,
                                                                     Double minLongitude,
                                                                     Double maxLongitude);
}
