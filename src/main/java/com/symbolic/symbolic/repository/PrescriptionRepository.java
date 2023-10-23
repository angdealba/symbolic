package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findPrescriptionsByPatientId(Long patientId);
    List<Prescription> findPrescriptionsByPractitionerId(Long practitionerId);
}
