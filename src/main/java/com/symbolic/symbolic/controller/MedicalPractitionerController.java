package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.AppointmentRepository;
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
public class MedicalPractitionerController {
    @Autowired
    MedicalPractitionerRepository practitionerRepository;
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    PrescriptionRepository prescriptionRepository;

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

    @GetMapping("/practitioner/appointments")
    public ResponseEntity<?> getAllAppointmentsByPractitionerId(@RequestParam("practitionerId") Long practitionerId) {
        if (!practitionerRepository.existsById(practitionerId)) {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentRepository.findAppointmentsByPractitionerId(practitionerId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/appointment/practitioner")
    public ResponseEntity<?> getPractitionerByAppointmentId(@RequestParam("appointmentId") Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            String errorMessage = "No appointment found with id " + appointmentId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        MedicalPractitioner practitioner = practitionerRepository.findMedicalPractitionerByAppointmentsId(appointmentId);
        return new ResponseEntity<>(practitioner, HttpStatus.OK);
    }

    @PostMapping("/practitioner/appointment")
    public ResponseEntity<?> addAppointmentToPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                @RequestParam("appointmentId") Long appointmentId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            Optional<Appointment> appointmentData = appointmentRepository.findById(appointmentId);

            if (appointmentData.isPresent()) {
                Appointment appointment = appointmentData.get();

                MedicalPractitioner oldPractitioner = practitionerRepository.findMedicalPractitionerByAppointmentsId(appointmentId);

                if (oldPractitioner != null) {
                    oldPractitioner.removeAppointmentById(appointmentId);
                    practitionerRepository.save(oldPractitioner);

                    practitioner.addAppointment(appointment);
                    practitionerRepository.save(practitioner);
                    return new ResponseEntity<>(appointment, HttpStatus.OK);
                } else {
                    practitioner.addAppointment(appointment);
                    practitionerRepository.save(practitioner);
                    return new ResponseEntity<>(appointment, HttpStatus.OK);
                }
            } else {
                String errorMessage = "No appointment found with id " + appointmentId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/practitioner/appointment")
    public ResponseEntity<?> removeAppointmentFromPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                     @RequestParam("appointmentId") Long appointmentId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            if (appointmentRepository.existsById(appointmentId)) {
                practitioner.removeAppointmentById(appointmentId);
                practitionerRepository.save(practitioner);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No appointment found with id " + appointmentId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/practitioner/prescriptions")
    public ResponseEntity<?> getAllPrescriptionsByPractitionerId(@RequestParam("practitionerId") Long practitionerId) {
        if (!practitionerRepository.existsById(practitionerId)) {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Prescription> prescriptions = prescriptionRepository.findPrescriptionsByPractitionerId(practitionerId);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/prescription/practitioner")
    public ResponseEntity<?> getPractitionerByPrescriptionId(@RequestParam("prescriptionId") Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            String errorMessage = "No prescription found with id " + prescriptionId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        MedicalPractitioner practitioner = practitionerRepository.findMedicalPractitionerByPrescriptionsId(prescriptionId);
        return new ResponseEntity<>(practitioner, HttpStatus.OK);
    }

    @PostMapping("/practitioner/prescription")
    public ResponseEntity<?> addPrescriptionToPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                          @RequestParam("prescriptionId") Long prescriptionId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            Optional<Prescription> prescriptionData = prescriptionRepository.findById(prescriptionId);

            if (prescriptionData.isPresent()) {
                Prescription prescription = prescriptionData.get();

                MedicalPractitioner oldPractitioner = practitionerRepository.findMedicalPractitionerByPrescriptionsId(prescriptionId);

                if (oldPractitioner != null) {
                    oldPractitioner.removePrescriptionById(prescriptionId);
                    practitionerRepository.save(oldPractitioner);

                    practitioner.addPrescription(prescription);
                    practitionerRepository.save(practitioner);
                    return new ResponseEntity<>(prescription, HttpStatus.OK);
                } else {
                    practitioner.addPrescription(prescription);
                    practitionerRepository.save(practitioner);
                    return new ResponseEntity<>(prescription, HttpStatus.OK);
                }
            } else {
                String errorMessage = "No prescription found with id " + prescriptionId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/practitioner/prescription")
    public ResponseEntity<?> removePrescriptionFromPractitioner(@RequestParam("practitionerId") Long practitionerId,
                                                               @RequestParam("prescriptionId") Long prescriptionId) {
        Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(practitionerId);

        if (practitionerData.isPresent()) {
            MedicalPractitioner practitioner = practitionerData.get();

            if (prescriptionRepository.existsById(prescriptionId)) {
                practitioner.removePrescriptionById(prescriptionId);
                practitionerRepository.save(practitioner);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No prescription found with id " + prescriptionId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No practitioner found with id " + practitionerId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }
}
