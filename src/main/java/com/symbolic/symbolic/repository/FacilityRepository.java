package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findFacilitiesByPatientsId(Long patientId);
    Facility findFacilityByPractitionersId(Long practitionerId);
}
