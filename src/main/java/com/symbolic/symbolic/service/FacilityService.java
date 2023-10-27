package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.repository.FacilityRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the functionality for performing a search of facilities.
 */
@Service
public class FacilityService {
  @Autowired
  private FacilityRepository facilityRepository;

  /**
   * Search for a facility based off of location and specialization.
   *
   * @param latitude double value for latitude
   * @param longitude double value for longitude
   * @param specialization string for specialization type
   * @return a list of Facility objects satisfying the search requirements
   */
  public List<Facility> searchFacility(Double latitude, Double longitude, String specialization) {
    Double latitudeThreshold = 0.1;
    Double longitudeThreshold = 0.1;

    Double minLatitude = latitude - latitudeThreshold;
    Double maxLatitude = latitude + latitudeThreshold;
    Double minLongitude = longitude - longitudeThreshold;
    Double maxLongitude = longitude + longitudeThreshold;


    List<Facility> facilities = facilityRepository.findByLatitudeBetweenAndLongitudeBetween(
        minLatitude, maxLatitude, minLongitude, maxLongitude
    );
    System.out.println(facilities.size());
    if (specialization != null) {
      facilities = facilities.stream().filter(facility ->
          facility.getSpecialization().equals(specialization)).collect(Collectors.toList());
    }
    return facilities;
  }
}
