package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Diagnosis;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Provides a set of search methods from Spring Boot for Diagnosis data.
 */
@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {
  List<Diagnosis> findDiagnosesByPatientId(UUID patientId);

  List<Diagnosis> findDiagnosesByPractitionerId(UUID practitionerId);

  List<Diagnosis> findDiagnosesByConditionIgnoreCase(String condition);
}
