package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {
    List<Facility> findFacilitiesByPatientsId(Long patientId);
    Facility findFacilityByPractitionersId(Long practitionerId);
    Facility findFacilityByAppointmentsId(Long appointmentId);
}
