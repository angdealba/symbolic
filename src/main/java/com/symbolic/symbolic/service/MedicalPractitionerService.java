package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the functionality for performing a search of medical practitioners.
 */
@Service
public class MedicalPractitionerService {
  @Autowired
  private MedicalPractitionerRepository medicalPractitionerRepository;

  /**
   * Search for a practitioner based off of location, specialization, cost, and experience.
   *
   * @param latitude double value for latitude
   * @param longitude double value for longitude
   * @param specialization string for specialization type
   * @param cost integer value for the cost of a consultation
   * @param experienceLevel integer value for the practitioner's years working
   * @return a list of MedicalPractitioner objects satisfying the search requirements
   */
  public List<MedicalPractitioner> search(Double latitude, Double longitude,
                                          String specialization, Integer cost,
                                          Integer experienceLevel) {
    // Define search criteria based on provided parameters
    Double latitudeThreshold = 0.1;
    Double longitudeThreshold = 0.1;

    Double minLatitude = latitude - latitudeThreshold;
    Double maxLatitude = latitude + latitudeThreshold;
    Double minLongitude = longitude - longitudeThreshold;
    Double maxLongitude = longitude + longitudeThreshold;

    List<MedicalPractitioner> practitioners = medicalPractitionerRepository
        .findByLatitudeBetweenAndLongitudeBetween(
        minLatitude, maxLatitude, minLongitude, maxLongitude
    );

    // Add filtering logic based on specialization, cost, and experienceLevel here
    if (specialization != null) {
      practitioners = practitioners.stream().filter(practitioner ->
          practitioner.getSpecialization().equals(specialization)).collect(Collectors.toList());
    }

    if (cost != null) {
      practitioners = practitioners.stream().filter(practitioner ->
          practitioner.getConsultationCost() <= cost).collect(Collectors.toList());
    }

    if (experienceLevel != null) {
      practitioners = practitioners.stream().filter(practitioner ->
          practitioner.getYearsExperience() >= experienceLevel).collect(Collectors.toList());
    }
    return practitioners;
  }


}