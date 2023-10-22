package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
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
public class MedicalPractitionerController {
    @Autowired
    MedicalPractitionerRepository practitionerRepository;

    @Autowired
    PatientRepository patientRepository;

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
                practitioner.getLatitude(), practitioner.getLongitude(), practitioner.getSpecialization(),
                practitioner.getConsultationCost(), practitioner.getYearsExperience()
        );
        practitionerRepository.save(newPractitioner);
        return new ResponseEntity<>(newPractitioner, HttpStatus.CREATED);
    }

    @PutMapping("/practitioner")
    public ResponseEntity<?> updatePractitioner(@RequestParam("id") Long id, @RequestBody MedicalPractitioner practitioner) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

        if (practitionerData.isPresent()) {
            MedicalPractitioner oldPractitioner = practitionerData.get();
            oldPractitioner.setLatitude(practitioner.getLatitude());
            oldPractitioner.setLongitude(practitioner.getLongitude());
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

    @GetMapping("/practitioner/patients")
    public ResponseEntity<?> getAllPatientsByPractitionerId(@RequestParam("practitionerId") Long practitionerId) {
        if (!practitionerRepository.existsById(practitionerId)) {
            String errorMessage = "No medical practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Patient> patients = patientRepository.findPatientsByPractitionersId(practitionerId);
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping("/patient/practitioners")
    public ResponseEntity<?> getAllPractitionersByPatientId(@RequestParam("patientId") Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<MedicalPractitioner> practitioners = practitionerRepository.findMedicalPractitionerByPatientsId(patientId);
        return new ResponseEntity<>(practitioners, HttpStatus.OK);
    }

//    @PostMapping("/practitioner/patient")
//    public ResponseEntity<?> addPatientToPractitioner(@RequestParam("id") Long practitionerId, @RequestBody Patient patient) {
//        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);
//
//        if (practitionerData.isPresent()) {
//            MedicalPractitioner practitioner = practitionerData.get();
//
//            long patientId = patient.getId();
//
//            if (patientId != 0L) {
//                Optional<Patient> patientData = patientRepository.findById(patientId);
//
//                if (patientData.isPresent()) {
//                    Patient oldPatient = patientData.get();
//
//                    practitioner.addPatient(oldPatient);
//                    practitionerRepository.save(practitioner);
//                    return new ResponseEntity<>(oldPatient, HttpStatus.OK);
//                } else {
//                    String errorMessage = "No patient found with id " + patientId;
//                    return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
//                }
//            }
//
//            practitioner.addPatient(patient);
//            practitionerRepository.save(practitioner);
//            return new ResponseEntity<>(patient, HttpStatus.OK);
//        } else {
//            String errorMessage = "No practitioner found with id " + practitionerId;
//            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
//        }
//    }

    @PostMapping("/practitioner/patient")
    public ResponseEntity<?> addPatientToPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                      @RequestParam("patientId") Long patientId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            Optional<Patient> patientData = patientRepository.findById(patientId);

            if (patientData.isPresent()) {
                Patient patient = patientData.get();

                practitioner.addPatient(patient);
                practitionerRepository.save(practitioner);
                return new ResponseEntity<>(patient, HttpStatus.OK);
            } else {
                String errorMessage = "No patient found with id " + patientId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/practitioner/patient")
    public ResponseEntity<?> removePatientFromPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                           @RequestParam("patientId") Long patientId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            if (patientRepository.existsById(patientId)) {
                practitioner.removePatientById(patientId);
                practitionerRepository.save(practitioner);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No patient found with id " + patientId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }
}
