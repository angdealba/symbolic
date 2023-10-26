package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class DiagnosisController {
    @Autowired
    DiagnosisRepository diagnosisRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    MedicalPractitionerRepository practitionerRepository;

    @GetMapping("/diagnoses")
    public ResponseEntity<?> getAllDiagnoses() {
        List<Diagnosis> diagnoses = new ArrayList<>();
        diagnoses.addAll(diagnosisRepository.findAll());

        if (diagnoses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(diagnoses, HttpStatus.OK);
    }

    @GetMapping("/diagnosis")
    public ResponseEntity<?> getDiagnosisById(@RequestParam("id") Long id) {
        Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

        if (diagnosisData.isPresent()) {
            Diagnosis diagnosis = diagnosisData.get();
            return new ResponseEntity<>(diagnosis, HttpStatus.OK);
        } else {
            String errorMessage = "No diagnosis found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/diagnosis")
    public ResponseEntity<?> createDiagnosis(@RequestBody Diagnosis diagnosis) {
        Diagnosis newDiagnosis = new Diagnosis(
                diagnosis.getCondition(), diagnosis.getTreatmentInfo(), diagnosis.getDate()
        );

        diagnosisRepository.save(newDiagnosis);
        return new ResponseEntity<>(newDiagnosis, HttpStatus.CREATED);
    }

    @PutMapping("/diagnosis")
    public ResponseEntity<?> updateDiagnosis(@RequestParam("id") Long id, @RequestBody Diagnosis diagnosis) {
        Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

        if (diagnosisData.isPresent()) {
            Diagnosis oldDiagnosis = diagnosisData.get();
            oldDiagnosis.setCondition(diagnosis.getCondition());
            oldDiagnosis.setTreatmentInfo(diagnosis.getTreatmentInfo());
            oldDiagnosis.setDate(diagnosis.getDate());
            diagnosisRepository.save(oldDiagnosis);

            return new ResponseEntity<>(oldDiagnosis, HttpStatus.OK);
        } else {
            String errorMessage = "No diagnosis found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/diagnosis")
    public ResponseEntity<?> deleteDiagnosis(@RequestParam("id") Long id) {
        Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

        if (diagnosisData.isPresent()) {
            Diagnosis diagnosis = diagnosisData.get();

            Patient patient = diagnosis.getPatient();
            if (patient != null) {
                patient.removeDiagnosisById(id);
                patientRepository.save(patient);
            }

            MedicalPractitioner practitioner = diagnosis.getPractitioner();
            if (practitioner != null) {
                practitioner.removeDiagnosisById(id);
                practitionerRepository.save(practitioner);
            }

            diagnosisRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No diagnosis found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/diagnoses")
    public ResponseEntity<?> deleteAllDiagnoses() {
        List<Diagnosis> diagnoses = diagnosisRepository.findAll();

        for (Diagnosis diagnosis : diagnoses) {
            Long id = diagnosis.getId();

            Patient patient = diagnosis.getPatient();
            if (patient != null) {
                patient.removeDiagnosisById(id);
                patientRepository.save(patient);
            }

            MedicalPractitioner practitioner = diagnosis.getPractitioner();
            if (practitioner != null) {
                practitioner.removeDiagnosisById(id);
                practitionerRepository.save(practitioner);
            }
        }

        diagnosisRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
