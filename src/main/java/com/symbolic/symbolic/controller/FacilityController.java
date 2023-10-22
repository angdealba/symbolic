package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class FacilityController {
    @Autowired
    FacilityRepository facilityRepository;
    @Autowired
    MedicalPractitionerRepository practitionerRepository;
    @Autowired
    PatientRepository patientRepository;

    @GetMapping("/facilities")
    public ResponseEntity<?> getAllFacilities() {
        List<Facility> facilities = new ArrayList<>();
        facilities.addAll(facilityRepository.findAll());

        if (facilities.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(facilities, HttpStatus.OK);
    }

    @GetMapping("/facility")
    public ResponseEntity<?> getFacilityById(@RequestParam("id") Long id) {
        Optional<Facility> facilityData = facilityRepository.findById(id);

        if (facilityData.isPresent()) {
            Facility facility = facilityData.get();
            return new ResponseEntity<>(facility, HttpStatus.OK);
        } else {
            String errorMessage = "No facility found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/facility")
    public ResponseEntity<?> createFacility(@RequestBody Facility facility) {
        Facility newFacility = new Facility(
                facility.getLatitude(), facility.getLongitude(), facility.getSpecialization()
        );
        facilityRepository.save(newFacility);
        return new ResponseEntity<>(newFacility, HttpStatus.CREATED);
    }

    @PutMapping("/facility")
    public ResponseEntity<?> updateFacility(@RequestParam("id") Long id, @RequestBody Facility facility) {
        Optional<Facility> facilityData = facilityRepository.findById(id);

        if (facilityData.isPresent()) {
            Facility oldFacility = facilityData.get();
            oldFacility.setLatitude(facility.getLatitude());
            oldFacility.setLongitude(facility.getLongitude());
            oldFacility.setSpecialization(facility.getSpecialization());
            facilityRepository.save(oldFacility);

            return new ResponseEntity<>(oldFacility, HttpStatus.OK);
        } else {
            String errorMessage = "No facility found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/facility")
    public ResponseEntity<?> deleteFacility(@RequestParam("id") Long id) {
        if (facilityRepository.existsById(id)) {
            facilityRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No facility found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/facilities")
    public ResponseEntity<?> deleteAllFacilities() {
        facilityRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
