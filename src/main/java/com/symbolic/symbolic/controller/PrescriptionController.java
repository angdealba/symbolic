package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import com.symbolic.symbolic.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class PrescriptionController {
    @Autowired
    PrescriptionRepository prescriptionRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    MedicalPractitionerRepository practitionerRepository;

    @GetMapping("/prescriptions")
    public ResponseEntity<?> getAllPrescriptions() {
        List<Prescription> prescriptions = new ArrayList<>();
        prescriptions.addAll(prescriptionRepository.findAll());

        if (prescriptions.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/prescription")
    public ResponseEntity<?> getPrescriptionById(@RequestParam("id") Long id) {
        Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

        if (prescriptionData.isPresent()) {
            Prescription prescription = prescriptionData.get();
            return new ResponseEntity<>(prescription, HttpStatus.OK);
        } else {
            String errorMessage = "No prescription found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/prescription")
    public ResponseEntity<?> createPrescription(@RequestBody Prescription prescription) {
        Prescription newPrescription = new Prescription(
                prescription.getDosage(), prescription.getDailyUses(), prescription.getCost(), prescription.getInstructions()
        );

        prescriptionRepository.save(newPrescription);
        return new ResponseEntity<>(newPrescription, HttpStatus.CREATED);
    }

    @PutMapping("/prescription")
    public ResponseEntity<?> updatePrescription(@RequestParam("id") Long id, @RequestBody Prescription prescription) {
        Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

        if (prescriptionData.isPresent()) {
            Prescription oldPrescription = prescriptionData.get();
            oldPrescription.setDosage(prescription.getDosage());
            oldPrescription.setDailyUses(prescription.getDailyUses());
            oldPrescription.setCost(prescription.getCost());
            oldPrescription.setInstructions(prescription.getInstructions());
            prescriptionRepository.save(oldPrescription);

            return new ResponseEntity<>(oldPrescription, HttpStatus.OK);
        } else {
            String errorMessage = "No prescription found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/prescription")
    public ResponseEntity<?> deletePrescription(@RequestParam("id") Long id) {
        Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

        if (prescriptionData.isPresent()) {
            Prescription prescription = prescriptionData.get();

            Patient patient = prescription.getPatient();
            if (patient != null) {
                patient.removeDiagnosisById(id);
                patientRepository.save(patient);
            }

            MedicalPractitioner practitioner = prescription.getPractitioner();
            if (practitioner != null) {
                practitioner.removeDiagnosisById(id);
                practitionerRepository.save(practitioner);
            }

            prescriptionRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No prescription found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/prescriptions")
    public ResponseEntity<?> deleteAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();

        for (Prescription prescription : prescriptions) {
            Long id = prescription.getId();

            Patient patient = prescription.getPatient();
            if (patient != null) {
                patient.removePrescriptionById(id);
                patientRepository.save(patient);
            }

            MedicalPractitioner practitioner = prescription.getPractitioner();
            if (practitioner != null) {
                practitioner.removePrescriptionById(id);
                practitionerRepository.save(practitioner);
            }
        }

        prescriptionRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
