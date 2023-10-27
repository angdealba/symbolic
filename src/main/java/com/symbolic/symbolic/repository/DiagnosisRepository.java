package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Diagnosis;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Provides a set of search methods from Spring Boot for Diagnosis data.
 */
@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
  List<Diagnosis> findDiagnosesByPatientId(Long patientId);

  List<Diagnosis> findDiagnosesByPractitionerId(Long practitionerId);

  List<Diagnosis> findDiagnosesByConditionIgnoreCase(String condition);
}
