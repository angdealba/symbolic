package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Prescription;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for Prescription data.
 */
@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
  List<Prescription> findPrescriptionsByPatientId(UUID patientId);

  List<Prescription> findPrescriptionsByPractitionerId(UUID practitionerId);
}
