package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.repository.FacilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacilityService {
    @Autowired
    private FacilityRepository facilityRepository;
    public List<Facility> searchFacility(Double latitude, Double longitude, String specialization){
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
        if (specialization != null){
            facilities = facilities.stream().filter(facility -> facility.getSpecialization().equals(specialization)).collect(Collectors.toList());
        }
        return facilities;
    }
}
