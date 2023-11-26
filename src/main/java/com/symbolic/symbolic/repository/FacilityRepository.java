package com.symbolic.symbolic.repository;

import com.symbolic.symbolic.entity.Facility;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Provides a set of search methods from Spring Boot for Facility data.
 */
@Repository
public interface FacilityRepository extends JpaRepository<Facility, UUID> {
  List<Facility> findFacilitiesByPatientsId(UUID patientId);

  Facility findFacilityByPractitionersId(UUID practitionerId);

  Facility findFacilityByAppointmentsId(UUID appointmentId);

  List<Facility> findByLatitudeBetweenAndLongitudeBetween(Double minLatitude, Double maxLatitude,
                                                          Double minLongitude, Double maxLongitude);

}
