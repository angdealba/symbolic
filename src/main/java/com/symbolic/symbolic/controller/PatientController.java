package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Appointment;
import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.InsurancePolicy;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.AppointmentRepository;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.repository.InsurancePolicyRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import com.symbolic.symbolic.repository.PrescriptionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the patient data API.
 */
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

  /**
   * RequestBody object used to represent Patient-related requests.
   */
  static class PatientRequestBody {
    String id;
    String vaccinations;
    String allergies;
    String accommodations;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getVaccinations() {
      return vaccinations;
    }

    public String getAllergies() {
      return allergies;
    }

    public String getAccommodations() {
      return accommodations;
    }
  }

  /**
   * RequestBody object used to represent Patient-Appointment join requests.
   */
  static class PatientAppointmentBody {
    String patientId;
    String appointmentId;

    public String getPatientId() {
      return patientId;
    }

    public String getAppointmentId() {
      return appointmentId;
    }
  }

  /**
   * RequestBody object used to represent Patient-Prescription join requests.
   */
  static class PatientPrescriptionBody {
    String patientId;
    String prescriptionId;

    public String getPatientId() {
      return patientId;
    }

    public String getPrescriptionId() {
      return prescriptionId;
    }
  }

  /**
   * RequestBody object used to represent Patient-Diagnosis join requests.
   */
  static class PatientDiagnosisBody {
    String patientId;
    String diagnosisId;

    public String getPatientId() {
      return patientId;
    }

    public String getDiagnosisId() {
      return diagnosisId;
    }
  }

  /**
   * Parses a string input into a UUID object type for use in database lookup operations.
   *
   * @param uuidString a string value representing the UUID in the HTTP request.
   * @return A valid UUID object if the string can be converted successfully, or null if it cannot.
   */
  private static UUID parseUuidFromString(String uuidString) {
    try {
      return UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Implements GET endpoint /patients for returning all data.
   */
  @GetMapping("/patients")
  public ResponseEntity<?> getAllPatients() {
    List<Patient> patients = new ArrayList<Patient>();
    patients.addAll(patientRepository.findAll());

    if (patients.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(patients, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /patient for returning data matching an id.
   */
  @GetMapping("/patient")
  public ResponseEntity<?> getPatientById(@RequestBody PatientRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Patient> patientData = patientRepository.findById(id);

    if (patientData.isPresent()) {
      Patient patient = patientData.get();
      return new ResponseEntity<>(patient, HttpStatus.OK);
    } else {
      String errorMessage = "No patient found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /patient for uploading data.
   */
  @PostMapping("/patient")
  public ResponseEntity<?> createPatient(@RequestBody PatientRequestBody requestBody) {
    if (requestBody.getVaccinations() == null) {
      String errorMessage = "Missing 'vaccinations' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAllergies() == null) {
      String errorMessage = "Missing 'allergies' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAccommodations() == null) {
      String errorMessage = "Missing 'accommodations' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Patient newPatient = new Patient(requestBody.getVaccinations(),
        requestBody.getAllergies(), requestBody.getAccommodations());
    patientRepository.save(newPatient);
    return new ResponseEntity<>(newPatient, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /patient for updating data matching an id.
   */
  @PutMapping("/patient")
  public ResponseEntity<?> updatePatient(@RequestBody PatientRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Patient> patientData = patientRepository.findById(id);

    if (patientData.isPresent()) {
      Patient oldPatient = patientData.get();

      if (requestBody.getVaccinations() != null) {
        oldPatient.setVaccinations(requestBody.getVaccinations());
      }

      if (requestBody.getAllergies() != null) {
        oldPatient.setAllergies(requestBody.getAllergies());
      }

      if (requestBody.getAccommodations() != null) {
        oldPatient.setAccommodations(requestBody.getAccommodations());
      }

      patientRepository.save(oldPatient);
      return new ResponseEntity<>(oldPatient, HttpStatus.OK);
    } else {
      String errorMessage = "No patient found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /patient for removing data matching an id.
   */
  @DeleteMapping("/patient")
  public ResponseEntity<?> deletePatient(@RequestBody PatientRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements DELETE endpoint /patients for removing all data.
   */
  @DeleteMapping("/patients")
  public ResponseEntity<?> deleteAllPatients() {
    List<Patient> patients = patientRepository.findAll();

    for (Patient patient : patients) {
      UUID id = patient.getId();

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

  /**
   * Implements GET endpoint /patient/appointments for returning data matching an id.
   */
  @GetMapping("/patient/appointments")
  public ResponseEntity<?> getAllAppointmentsByPatientId(
      @RequestBody PatientAppointmentBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!patientRepository.existsById(patientId)) {
      String errorMessage = "No patient found with id " + patientId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Appointment> appointments = appointmentRepository.findAppointmentsByPatientId(patientId);
    return new ResponseEntity<>(appointments, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /appointment/patient for returning data matching an id.
   */
  @GetMapping("/appointment/patient")
  public ResponseEntity<?> getPatientByAppointmentId(
      @RequestBody PatientAppointmentBody requestBody) {
    if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!appointmentRepository.existsById(appointmentId)) {
      String errorMessage = "No appointment found with id " + appointmentId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    Patient patient = patientRepository.findPatientByAppointmentsId(appointmentId);
    return new ResponseEntity<>(patient, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @PostMapping("/patient/appointment")
  public ResponseEntity<?> addAppointmentToPatient(
      @RequestBody PatientAppointmentBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @DeleteMapping("/patient/appointment")
  public ResponseEntity<?> removeAppointmentFromPatient(
      @RequestBody PatientAppointmentBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @GetMapping("/patient/prescriptions")
  public ResponseEntity<?> getAllPrescriptionsByPatientId(
      @RequestBody PatientPrescriptionBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!patientRepository.existsById(patientId)) {
      String errorMessage = "No patient found with id " + patientId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Prescription> prescriptions
        = prescriptionRepository.findPrescriptionsByPatientId(patientId);
    return new ResponseEntity<>(prescriptions, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @GetMapping("/prescription/patient")
  public ResponseEntity<?> getPatientByPrescriptionId(
      @RequestBody PatientPrescriptionBody requestBody) {
    if (requestBody.getPrescriptionId() == null) {
      String errorMessage = "Missing 'prescriptionId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID prescriptionId = parseUuidFromString(requestBody.getPrescriptionId());
    if (prescriptionId == null) {
      String errorMessage = "'prescriptionId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!prescriptionRepository.existsById(prescriptionId)) {
      String errorMessage = "No prescription found with id " + prescriptionId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    Patient patient = patientRepository.findPatientByPrescriptionsId(prescriptionId);
    return new ResponseEntity<>(patient, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @PostMapping("/patient/prescription")
  public ResponseEntity<?> addPrescriptionToPatient(
      @RequestBody PatientPrescriptionBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPrescriptionId() == null) {
      String errorMessage = "Missing 'prescriptionId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID prescriptionId = parseUuidFromString(requestBody.getPrescriptionId());
    if (prescriptionId == null) {
      String errorMessage = "'prescriptionId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @DeleteMapping("/patient/prescription")
  public ResponseEntity<?> removePrescriptionFromPatient(
      @RequestBody PatientPrescriptionBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPrescriptionId() == null) {
      String errorMessage = "Missing 'prescriptionId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID prescriptionId = parseUuidFromString(requestBody.getPrescriptionId());
    if (prescriptionId == null) {
      String errorMessage = "'prescriptionId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @GetMapping("/patient/diagnoses")
  public ResponseEntity<?> getAllDiagnosesByPatientId(
      @RequestBody PatientDiagnosisBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!patientRepository.existsById(patientId)) {
      String errorMessage = "No patient found with id " + patientId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByPatientId(patientId);
    return new ResponseEntity<>(diagnoses, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @GetMapping("/diagnosis/patient")
  public ResponseEntity<?> getPatientByDiagnosisId(
      @RequestBody PatientDiagnosisBody requestBody) {
    if (requestBody.getDiagnosisId() == null) {
      String errorMessage = "Missing 'diagnosisId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID diagnosisId = parseUuidFromString(requestBody.getDiagnosisId());
    if (diagnosisId == null) {
      String errorMessage = "'diagnosisId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!diagnosisRepository.existsById(diagnosisId)) {
      String errorMessage = "No diagnosis found with id " + diagnosisId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    Patient patient = patientRepository.findPatientByDiagnosesId(diagnosisId);
    return new ResponseEntity<>(patient, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @PostMapping("/patient/diagnosis")
  public ResponseEntity<?> addDiagnosisToPatient(@RequestBody PatientDiagnosisBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDiagnosisId() == null) {
      String errorMessage = "Missing 'diagnosisId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID diagnosisId = parseUuidFromString(requestBody.getDiagnosisId());
    if (diagnosisId == null) {
      String errorMessage = "'diagnosisId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @DeleteMapping("/patient/diagnosis")
  public ResponseEntity<?> removeDiagnosisFromPatient(
      @RequestBody PatientDiagnosisBody requestBody) {
    if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDiagnosisId() == null) {
      String errorMessage = "Missing 'diagnosisId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID diagnosisId = parseUuidFromString(requestBody.getDiagnosisId());
    if (diagnosisId == null) {
      String errorMessage = "'diagnosisId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

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
