package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api")
public class PatientController {
    @Autowired
    PatientRepository patientRepository;
    @Autowired
    MedicalPractitionerRepository practitionerRepository;
    @Autowired
    FacilityRepository facilityRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    PrescriptionRepository prescriptionRepository;
    @Autowired
    DiagnosisRepository diagnosisRepository;
    @Autowired
    InsurancePolicyRepository insurancePolicyRepository;

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
        Optional<Patient> patientData = patientRepository.findById(id);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            Set<MedicalPractitioner> practitioners = patient.getPractitioners();
            for (MedicalPractitioner practitioner : practitioners) {
                practitioner.removePatientById(id);
                practitionerRepository.save(practitioner);
            }

            Set<Facility> facilities = patient.getFacilities();
            for (Facility facility : facilities) {
                facility.removePatientById(id);
                facilityRepository.save(facility);
            }

            InsurancePolicy policy = patient.getInsurancePolicy();
            if (policy != null) {
                policy.removePatientById(id);
                insurancePolicyRepository.save(policy);
            }

            Set<Appointment> appointments = patient.getAppointments();
            for (Appointment appointment : appointments) {
                appointment.setPractitioner(null);
                appointmentRepository.save(appointment);
            }

            Set<Prescription> prescriptions = patient.getPrescriptions();
            for (Prescription prescription : prescriptions) {
                prescription.setPractitioner(null);
                prescriptionRepository.save(prescription);
            }

            Set<Diagnosis> diagnoses = patient.getDiagnoses();
            for (Diagnosis diagnosis : diagnoses) {
                diagnosis.setPractitioner(null);
                diagnosisRepository.save(diagnosis);
            }

            patientRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            String errorMessage = "No patient found with id " + id;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patients")
    public ResponseEntity<?> deleteAllPatients() {
        List<Patient> patients = patientRepository.findAll();

        for (Patient patient : patients) {
            Long id = patient.getId();

            Set<MedicalPractitioner> practitioners = patient.getPractitioners();
            for (MedicalPractitioner practitioner : practitioners) {
                practitioner.removePatientById(id);
                practitionerRepository.save(practitioner);
            }

            Set<Facility> facilities = patient.getFacilities();
            for (Facility facility : facilities) {
                facility.removePatientById(id);
                facilityRepository.save(facility);
            }

            InsurancePolicy policy = patient.getInsurancePolicy();
            if (policy != null) {
                policy.removePatientById(id);
                insurancePolicyRepository.save(policy);
            }

            Set<Appointment> appointments = patient.getAppointments();
            for (Appointment appointment : appointments) {
                appointment.setPractitioner(null);
                appointmentRepository.save(appointment);
            }

            Set<Prescription> prescriptions = patient.getPrescriptions();
            for (Prescription prescription : prescriptions) {
                prescription.setPractitioner(null);
                prescriptionRepository.save(prescription);
            }

            Set<Diagnosis> diagnoses = patient.getDiagnoses();
            for (Diagnosis diagnosis : diagnoses) {
                diagnosis.setPractitioner(null);
                diagnosisRepository.save(diagnosis);
            }
        }

        patientRepository.deleteAll();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/patient/appointments")
    public ResponseEntity<?> getAllAppointmentsByPatientId(@RequestParam("patientId") Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Appointment> appointments = appointmentRepository.findAppointmentsByPatientId(patientId);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

    @GetMapping("/appointment/patient")
    public ResponseEntity<?> getPatientByAppointmentId(@RequestParam("appointmentId") Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            String errorMessage = "No appointment found with id " + appointmentId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientRepository.findPatientByAppointmentsId(appointmentId);
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    @PostMapping("/patient/appointment")
    public ResponseEntity<?> addAppointmentToPatient(@RequestParam("patientId") Long patientId,
                                                          @RequestParam("appointmentId") Long appointmentId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            Optional<Appointment> appointmentData = appointmentRepository.findById(appointmentId);

            if (appointmentData.isPresent()) {
                Appointment appointment = appointmentData.get();

                Patient oldPatient = patientRepository.findPatientByAppointmentsId(appointmentId);

                if (oldPatient != null) {
                    oldPatient.removeAppointmentById(appointmentId);
                    patientRepository.save(oldPatient);

                    patient.addAppointment(appointment);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(appointment, HttpStatus.OK);
                } else {
                    patient.addAppointment(appointment);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(appointment, HttpStatus.OK);
                }
            } else {
                String errorMessage = "No appointment found with id " + appointmentId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patient/appointment")
    public ResponseEntity<?> removeAppointmentFromPractitioner(@RequestParam("patientId") Long patientId,
                                                               @RequestParam("appointmentId") Long appointmentId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            if (appointmentRepository.existsById(appointmentId)) {
                patient.removeAppointmentById(appointmentId);
                patientRepository.save(patient);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No appointment found with id " + appointmentId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/patient/prescriptions")
    public ResponseEntity<?> getAllPrescriptionsByPatientId(@RequestParam("patientId") Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Prescription> prescriptions = prescriptionRepository.findPrescriptionsByPatientId(patientId);
        return new ResponseEntity<>(prescriptions, HttpStatus.OK);
    }

    @GetMapping("/prescription/patient")
    public ResponseEntity<?> getPatientByPrescriptionId(@RequestParam("prescriptionId") Long prescriptionId) {
        if (!prescriptionRepository.existsById(prescriptionId)) {
            String errorMessage = "No prescription found with id " + prescriptionId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientRepository.findPatientByPrescriptionsId(prescriptionId);
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    @PostMapping("/patient/prescription")
    public ResponseEntity<?> addPrescriptionToPatient(@RequestParam("patientId") Long patientId,
                                                     @RequestParam("prescriptionId") Long prescriptionId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            Optional<Prescription> prescriptionData = prescriptionRepository.findById(prescriptionId);

            if (prescriptionData.isPresent()) {
                Prescription prescription = prescriptionData.get();

                Patient oldPatient = patientRepository.findPatientByPrescriptionsId(prescriptionId);

                if (oldPatient != null) {
                    oldPatient.removePrescriptionById(prescriptionId);
                    patientRepository.save(oldPatient);

                    patient.addPrescription(prescription);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(prescription, HttpStatus.OK);
                } else {
                    patient.addPrescription(prescription);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(prescription, HttpStatus.OK);
                }
            } else {
                String errorMessage = "No prescription found with id " + prescriptionId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patient/prescription")
    public ResponseEntity<?> removePrescriptionFromPractitioner(@RequestParam("patientId") Long patientId,
                                                               @RequestParam("prescriptionId") Long prescriptionId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            if (prescriptionRepository.existsById(prescriptionId)) {
                patient.removePrescriptionById(prescriptionId);
                patientRepository.save(patient);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No prescription found with id " + prescriptionId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/patient/diagnoses")
    public ResponseEntity<?> getAllDiagnosesByPatientId(@RequestParam("patientId") Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByPatientId(patientId);
        return new ResponseEntity<>(diagnoses, HttpStatus.OK);
    }

    @GetMapping("/diagnosis/patient")
    public ResponseEntity<?> getPatientByDiagnosisId(@RequestParam("diagnosisId") Long diagnosisId) {
        if (!diagnosisRepository.existsById(diagnosisId)) {
            String errorMessage = "No diagnosis found with id " + diagnosisId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }

        Patient patient = patientRepository.findPatientByDiagnosesId(diagnosisId);
        return new ResponseEntity<>(patient, HttpStatus.OK);
    }

    @PostMapping("/patient/diagnosis")
    public ResponseEntity<?> addDiagnosisToPatient(@RequestParam("patientId") Long patientId,
                                                      @RequestParam("diagnosisId") Long diagnosisId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(diagnosisId);

            if (diagnosisData.isPresent()) {
                Diagnosis diagnosis = diagnosisData.get();

                Patient oldPatient = patientRepository.findPatientByDiagnosesId(diagnosisId);

                if (oldPatient != null) {
                    oldPatient.removeDiagnosisById(diagnosisId);
                    patientRepository.save(oldPatient);

                    patient.addDiagnosis(diagnosis);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(diagnosis, HttpStatus.OK);
                } else {
                    patient.addDiagnosis(diagnosis);
                    patientRepository.save(patient);
                    return new ResponseEntity<>(diagnosis, HttpStatus.OK);
                }
            } else {
                String errorMessage = "No diagnosis found with id " + diagnosisId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/patient/diagnosis")
    public ResponseEntity<?> removeDiagnosisFromPractitioner(@RequestParam("patientId") Long patientId,
                                                                @RequestParam("diagnosisId") Long diagnosisId) {
        Optional<Patient> patientData = patientRepository.findById(patientId);

        if (patientData.isPresent()) {
            Patient patient = patientData.get();

            if (diagnosisRepository.existsById(diagnosisId)) {
                patient.removeDiagnosisById(diagnosisId);
                patientRepository.save(patient);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                String errorMessage = "No diagnosis found with id " + diagnosisId;
                return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
            }
        } else {
            String errorMessage = "No patient found with id " + patientId;
            return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
        }
    }
}
