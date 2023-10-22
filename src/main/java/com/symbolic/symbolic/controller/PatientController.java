package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PatientController {
    @Autowired
    PatientRepository patientRepository;

    @GetMapping("/patients")
    public ResponseEntity<?> getAllPatients() {
        List<Patient> patients = new ArrayList<Patient>();
        patients.addAll(patientRepository.findAll());

        if (patients.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping("/patient")
    public ResponseEntity<?> getPatientById(@RequestParam("id") Long id) {
        Optional<Patient> patientData = patientRepository.findById(id);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();
            return new ResponseEntity<>(patient, HttpStatus.OK);
        } else {
            String errorMessage = "No patient found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/patient")
    public ResponseEntity<?> createPatient(@RequestBody Patient patient) {
        Patient newPatient = new Patient(patient.getVaccinations(), patient.getAllergies(), patient.getAccommodations());
        patientRepository.save(newPatient);
        return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
    }

    @PutMapping("/patient")
    public ResponseEntity<?> updatePatient(@RequestParam("id") Long id, @RequestBody Patient patient) {
        Optional<Patient> patientData = patientRepository.findById(id);

        if (patientData.isPresent()) {
            Patient oldPatient = patientData.get();
            oldPatient.setVaccinations(patient.getVaccinations());
            oldPatient.setAllergies(patient.getAllergies());
            oldPatient.setAccommodations(patient.getAccommodations());
            patientRepository.save(oldPatient);

            return new ResponseEntity<>(oldPatient, HttpStatus.OK);
        } else {
            String errorMessage = "No patient found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patient")
    public ResponseEntity<?> deletePatient(@RequestParam("id") Long id) {
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No patient found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patients")
    public ResponseEntity<?> deleteAllPatients() {
        patientRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
