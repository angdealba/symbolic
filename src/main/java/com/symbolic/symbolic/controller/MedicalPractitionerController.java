package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MedicalPractitionerController {
    @Autowired
    MedicalPractitionerRepository practitionerRepository;

    @GetMapping("/practitioners")
    public ResponseEntity<?> getAllPractitioners() {
        List<MedicalPractitioner> practitioners = new ArrayList<>();
        practitioners.addAll(practitionerRepository.findAll());

        if (practitioners.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(practitioners, HttpStatus.OK);
    }

    @GetMapping("/practitioner")
    public ResponseEntity<?> getPractitionerById(@RequestParam("id") Long id) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();
            return new ResponseEntity<>(practitioner, HttpStatus.OK);
        } else {
            String errorMessage = "No medical practitioner found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/practitioner")
    public ResponseEntity<?> createPractitioner(@RequestBody MedicalPractitioner practitioner) {
        MedicalPractitioner newPractitioner = new MedicalPractitioner(
                practitioner.getLongitude(), practitioner.getLatitude(), practitioner.getSpecialization(),
                practitioner.getConsultationCost(), practitioner.getYearsExperience()
        );
        practitionerRepository.save(newPractitioner);
        return new ResponseEntity<>(newPractitioner, HttpStatus.CREATED);
    }

    @PutMapping("/practitioner")
    public ResponseEntity<?> updatePatient(@RequestParam("id") Long id, @RequestBody MedicalPractitioner practitioner) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

        if (practitionerData.isPresent()) {
            MedicalPractitioner oldPractitioner = practitionerData.get();
            oldPractitioner.setLongitude(practitioner.getLongitude());
            oldPractitioner.setLatitude(practitioner.getLatitude());
            oldPractitioner.setSpecialization(practitioner.getSpecialization());
            oldPractitioner.setConsultationCost(practitioner.getConsultationCost());
            oldPractitioner.setYearsExperience(practitioner.getYearsExperience());
            practitionerRepository.save(oldPractitioner);

            return new ResponseEntity<>(oldPractitioner, HttpStatus.OK);
        } else {
            String errorMessage = "No practitioner found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/practitioner")
    public ResponseEntity<?> deletePractitioner(@RequestParam("id") Long id) {
        if (practitionerRepository.existsById(id)) {
            practitionerRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No practitioner found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/practitioners")
    public ResponseEntity<?> deleteAllPractitioners() {
        practitionerRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
