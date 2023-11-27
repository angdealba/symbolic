package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;


/**
 * Implements the functionality for retrieving historical diagnosis trends.
 */
@Service
@RestController("HistoricalDataService")
public class HistoricalDataService {

  @Autowired
  DiagnosisRepository diagnosisRepository;
  @Autowired
  MedicalPractitionerRepository medicalPractitionerRepository;

  /**
   * Retrieves a list of diagnoses matching a specific condition over a time range.
   *
   * @param condition string value for condition name
   * @param startDate Date value for the start of the search period
   * @param endDate   Date value of the end of the search period
   * @param location  List of Strings containing the coordinates of the location
   * @return a list of diagnoses matching the search terms
   */
  public List<Diagnosis> getHistoricalDataByCondition(String condition, Date startDate,
                                                      Date endDate, List<Double> location) {
    //search for condition
    List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByConditionIgnoreCase(condition);

    if (!diagnoses.isEmpty()) {
      diagnoses = filter(diagnoses, startDate, endDate, location);
    }
    return diagnoses;

  }


  /**
   * Performs the function of filtering a list of diagnoses to just the diagnoses
   * that match the search parameters.
   *
   * @param diagnoses an input list of diagnoses
   * @param startDate Date value for the start of the search period
   * @param endDate   Date value of the end of the search period
   * @param location  List of Strings containing the coordinates of the location
   * @return a list of diagnoses from the original list that match the search params
   */
  public List<Diagnosis> filter(List<Diagnosis> diagnoses, Date startDate,
                                Date endDate, List<Double> location) {
    //filter by date
    diagnoses = diagnoses.stream().filter(diagnosis ->
        diagnosis.getDate().after(startDate)
            && diagnosis.getDate().before(endDate)).collect(Collectors.toList());

    //filter by location
    List<Diagnosis> finalDiagnoses = diagnoses;
    if (location != null) {
      if (!location.isEmpty()) {
        Double latitudeThreshold = 0.1;
        Double longitudeThreshold = 0.1;

        Double minLatitude = location.get(0) - latitudeThreshold;
        Double maxLatitude = location.get(0) + latitudeThreshold;
        Double minLongitude = location.get(1) - longitudeThreshold;
        Double maxLongitude = location.get(1) + longitudeThreshold;

        List<MedicalPractitioner> practitioners = medicalPractitionerRepository
            .findByLatitudeBetweenAndLongitudeBetween(
                minLatitude, maxLatitude, minLongitude, maxLongitude
            );

        List<Diagnosis> filteredDiagnoses = practitioners.stream()
            .flatMap(practitioner -> practitioner.getDiagnoses().stream())
            .filter(finalDiagnoses::contains)
            .toList();

      }
    }
    return finalDiagnoses;
  }

  /**
   * Retrieves the N most common conditions over a time range and location.
   *
   * @param location  List of Strings containing the coordinates of the location
   * @param startDate Date value for the start of the search period
   * @param endDate   Date value of the end of the search period
   * @param n         integer value for the number of conditions to return
   * @return a map of condition names to frequencies
   */
  public Map<String, Integer> getTopConditions(List<Double> location, Date startDate,
                                               Date endDate, int n) {
    List<Diagnosis> diagnoses = new ArrayList<>(diagnosisRepository.findAll());

    diagnoses = filter(diagnoses, startDate, endDate, location);
    Map<String, Integer> counts = new HashMap<>();
    for (Diagnosis d : diagnoses) {
      String cond = d.getCondition();
      counts.put(cond, counts.getOrDefault(cond, 0) + 1);
    }

    //alg from https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
    return counts.entrySet().stream()
        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
        .limit(n)
        .collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));


  }
}
