package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.ArrayList;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Service

public class HistoricalDataService {

    @Autowired
    DiagnosisRepository diagnosisRepository;
    MedicalPractitionerRepository medicalPractitionerRepository;

    public List<Diagnosis> getHistoricalDataByCondition(String condition, Date startDate, Date endDate, List<String> location) {
        //search for condition
        List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByConditionIgnoreCase(condition);
        if(!diagnoses.isEmpty()) {
            diagnoses = filter(diagnoses, startDate, endDate, location);
        }
        return diagnoses;

    }

    public List<Diagnosis> filter(List<Diagnosis> diagnoses, Date startDate, Date endDate, List<String> location) {
        //filter by date
        diagnoses = diagnoses.stream().filter(diagnosis -> diagnosis.getDate().after(startDate) && diagnosis.getDate().before(endDate)).collect(Collectors.toList());

        //filter by location
        if (!location.isEmpty()) {
            Double latitudeThreshold = 0.1;
            Double longitudeThreshold = 0.1;

            Double minLatitude = Integer.parseInt(location.get(0)) - latitudeThreshold;
            Double maxLatitude = Integer.parseInt(location.get(0)) + latitudeThreshold;
            Double minLongitude = Integer.parseInt(location.get(0)) - longitudeThreshold;
            Double maxLongitude = Integer.parseInt(location.get(0)) + longitudeThreshold;

            List<MedicalPractitioner> practitioners = medicalPractitionerRepository.findByLatitudeBetweenAndLongitudeBetween(
                    minLatitude, maxLatitude, minLongitude, maxLongitude
            );
            diagnoses = diagnoses.stream().filter(diagnosis -> practitioners.contains(diagnosis.getPractitioner())).collect(Collectors.toList());

        }
        return diagnoses;
    }

    public Map<String, Integer> getTopConditions(List<String> location, Date startDate, Date endDate, int N) {
        List<Diagnosis> diagnoses = new ArrayList<>(diagnosisRepository.findAll());

        diagnoses = filter(diagnoses, startDate, endDate, location);
        Map<String, Integer> counts = new HashMap<>();
        for (Diagnosis d : diagnoses) {
            String cond = d.getCondition();
            counts.put(cond, counts.getOrDefault(cond, 0) + 1);
        }

        //alg from https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values
        Map<String, Integer> topN =
                counts.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(N)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        return topN;
    }
}
