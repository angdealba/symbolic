package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    List<Diagnosis> findDiagnosesByPatientId(Long patientId);
    List<Diagnosis> findDiagnosesByPractitionerId(Long practitionerId);
}
