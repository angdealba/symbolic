package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Prescription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for Prescription data.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
  List<Prescription> findPrescriptionsByPatientId(Long patientId);

  List<Prescription> findPrescriptionsByPractitionerId(Long practitionerId);
}
