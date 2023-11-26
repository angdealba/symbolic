package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for MedicalPractitioner data.
 */
@Repository
public interface MedicalPractitionerRepository extends JpaRepository<MedicalPractitioner, UUID> {
  List<MedicalPractitioner> findMedicalPractitionerByPatientsId(UUID patientId);

  List<MedicalPractitioner> findMedicalPractitionerByFacilityId(UUID facilityId);

  MedicalPractitioner findMedicalPractitionerByAppointmentsId(UUID appointmentId);

  MedicalPractitioner findMedicalPractitionerByPrescriptionsId(UUID prescriptionId);

  MedicalPractitioner findMedicalPractitionerByDiagnosesId(UUID diagnosisId);

  List<MedicalPractitioner> findByLatitudeBetweenAndLongitudeBetween(Double minLatitude,
                                                                     Double maxLatitude,
                                                                     Double minLongitude,
                                                                     Double maxLongitude);
}
