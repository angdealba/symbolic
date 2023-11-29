package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Appointment;
import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.AppointmentRepository;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import com.symbolic.symbolic.repository.PrescriptionRepository;
import com.symbolic.symbolic.service.MedicalPractitionerService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the practitioner data API.
 */
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
  @Autowired
  DiagnosisRepository diagnosisRepository;
  @Autowired
  FacilityRepository facilityRepository;

  @Autowired
  private MedicalPractitionerService medicalPractitionerService;

  /**
   * RequestBody object used to represent Patient-related requests.
   */
  static class PractitionerRequestBody {
    String id;
    Double latitude;
    Double longitude;
    String specialization;
    Integer consultationCost;
    Integer yearsExperience;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public String getSpecialization() {
      return specialization;
    }

    public Integer getConsultationCost() {
      return consultationCost;
    }

    public Integer getYearsExperience() {
      return yearsExperience;
    }
  }

  /**
   * RequestBody object used to represent Practitioner-Patient join requests.
   */
  static class PractitionerPatientBody {
    String practitionerId;
    String patientId;

    public String getPractitionerId() {
      return practitionerId;
    }

    public String getPatientId() {
      return patientId;
    }
  }

  /**
   * RequestBody object used to represent Practitioner-Appointment join requests.
   */
  static class PractitionerAppointmentBody {
    String practitionerId;
    String appointmentId;

    public String getPractitionerId() {
      return practitionerId;
    }

    public String getAppointmentId() {
      return appointmentId;
    }
  }

  /**
   * RequestBody object used to represent Practitioner-Prescription join requests.
   */
  static class PractitionerPrescriptionBody {
    String practitionerId;
    String prescriptionId;

    public String getPractitionerId() {
      return practitionerId;
    }

    public String getPrescriptionId() {
      return prescriptionId;
    }
  }

  /**
   * RequestBody object used to represent Practitioner-Diagnosis join requests.
   */
  static class PractitionerDiagnosisBody {
    String practitionerId;
    String diagnosisId;

    public String getPractitionerId() {
      return practitionerId;
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
  public static UUID parseUuidFromString(String uuidString) {
    try {
      return UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Implements GET endpoint /practitioners for returning all data.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/practitioners")
  public ResponseEntity<?> getAllPractitioners() {
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    practitioners.addAll(practitionerRepository.findAll());

    if (practitioners.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(practitioners, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /practitioner for returning data matching an id.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/practitioner")
  public ResponseEntity<?> getPractitionerById(@RequestBody PractitionerRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();
      return new ResponseEntity<>(practitioner, HttpStatus.OK);
    } else {
      String errorMessage = "No medical practitioner found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /practitioner for uploading data.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/practitioner")
  public ResponseEntity<?> createPractitioner(@RequestBody PractitionerRequestBody requestBody) {
    if (requestBody.getLatitude() == null) {
      String errorMessage = "Missing 'latitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getLongitude() == null) {
      String errorMessage = "Missing 'longitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getSpecialization() == null) {
      String errorMessage = "Missing 'specialization' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getConsultationCost() == null) {
      String errorMessage = "Missing 'consultationCost' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getYearsExperience() == null) {
      String errorMessage = "Missing 'yearsExperience' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getConsultationCost() < 0) {
      String errorMessage = "'consultationCost' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getYearsExperience() < 0) {
      String errorMessage = "'yearsExperience' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    MedicalPractitioner newPractitioner = new MedicalPractitioner(
        requestBody.getLatitude(), requestBody.getLongitude(), requestBody.getSpecialization(),
        requestBody.getConsultationCost(), requestBody.getYearsExperience()
    );
    practitionerRepository.save(newPractitioner);
    return new ResponseEntity<>(newPractitioner, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /practitioner for updating data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @PutMapping("/practitioner")
  public ResponseEntity<?> updatePractitioner(@RequestBody PractitionerRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

    if (practitionerData.isPresent()) {
      MedicalPractitioner oldPractitioner = practitionerData.get();

      if (requestBody.getLatitude() != null) {
        oldPractitioner.setLatitude(requestBody.getLatitude());
      }

      if (requestBody.getLongitude() != null) {
        oldPractitioner.setLongitude(requestBody.getLongitude());
      }

      if (requestBody.getSpecialization() != null) {
        oldPractitioner.setSpecialization(requestBody.getSpecialization());
      }

      if (requestBody.getConsultationCost() != null) {
        if (requestBody.getConsultationCost() < 0) {
          String errorMessage = "'consultationCost' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPractitioner.setConsultationCost(requestBody.getConsultationCost());
        }
      }

      if (requestBody.getYearsExperience() != null) {
        if (requestBody.getYearsExperience() < 0) {
          String errorMessage = "'yearsExperience' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPractitioner.setYearsExperience(requestBody.getYearsExperience());
        }
      }

      practitionerRepository.save(oldPractitioner);
      return new ResponseEntity<>(oldPractitioner, HttpStatus.OK);
    } else {
      String errorMessage = "No medical practitioner found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /practitioner for removing data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioner")
  public ResponseEntity<?> deletePractitioner(@RequestBody PractitionerRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData = practitionerRepository.findById(id);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();

      Set<Patient> patients = practitioner.getPatients();
      for (Patient patient : patients) {
        patient.removePractitionerById(id);
        patientRepository.save(patient);
      }

      Facility facility = practitioner.getFacility();
      if (facility != null) {
        facility.removePractitionerById(id);
        facilityRepository.save(facility);
      }

      Set<Appointment> appointments = practitioner.getAppointments();
      for (Appointment appointment : appointments) {
        appointment.setPractitioner(null);
        appointmentRepository.save(appointment);
      }

      Set<Prescription> prescriptions = practitioner.getPrescriptions();
      for (Prescription prescription : prescriptions) {
        prescription.setPractitioner(null);
        prescriptionRepository.save(prescription);
      }

      Set<Diagnosis> diagnoses = practitioner.getDiagnoses();
      for (Diagnosis diagnosis : diagnoses) {
        diagnosis.setPractitioner(null);
        diagnosisRepository.save(diagnosis);
      }

      practitionerRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No medical practitioner found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /practitioners for removing all data.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioners")
  public ResponseEntity<?> deleteAllPractitioners() {
    List<MedicalPractitioner> practitioners = practitionerRepository.findAll();

    for (MedicalPractitioner practitioner : practitioners) {
      UUID id = practitioner.getId();

      Set<Patient> patients = practitioner.getPatients();
      for (Patient patient : patients) {
        patient.removePractitionerById(id);
        patientRepository.save(patient);
      }

      Facility facility = practitioner.getFacility();
      if (facility != null) {
        facility.removePractitionerById(id);
        facilityRepository.save(facility);
      }

      Set<Appointment> appointments = practitioner.getAppointments();
      for (Appointment appointment : appointments) {
        appointment.setPractitioner(null);
        appointmentRepository.save(appointment);
      }

      Set<Prescription> prescriptions = practitioner.getPrescriptions();
      for (Prescription prescription : prescriptions) {
        prescription.setPractitioner(null);
        prescriptionRepository.save(prescription);
      }

      Set<Diagnosis> diagnoses = practitioner.getDiagnoses();
      for (Diagnosis diagnosis : diagnoses) {
        diagnosis.setPractitioner(null);
        diagnosisRepository.save(diagnosis);
      }
    }

    practitionerRepository.deleteAll();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Implements GET endpoint /practitioners/search for returning practitioners
   * within a radius of the location.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/practitioners/search")
  public ResponseEntity<?> search(@RequestBody PractitionerRequestBody requestBody) {
    if (requestBody.getLatitude() == null) {
      String errorMessage = "Missing 'latitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getLongitude() == null) {
      String errorMessage = "Missing 'longitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(
        medicalPractitionerService
            .search(requestBody.getLatitude(), requestBody.getLongitude(),
                requestBody.getSpecialization(), requestBody.getConsultationCost(),
                requestBody.getYearsExperience()),
        HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /practitioner/patients for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/practitioner/patients")
  public ResponseEntity<?> getAllPatientsByPractitionerId(
      @RequestBody PractitionerPatientBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!practitionerRepository.existsById(practitionerId)) {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Patient> patients = patientRepository.findPatientsByPractitionersId(practitionerId);
    return new ResponseEntity<>(patients, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /patient/practitioners for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/patient/practitioners")
  public ResponseEntity<?> getAllPractitionersByPatientId(
      @RequestBody PractitionerPatientBody requestBody) {
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

    List<MedicalPractitioner> practitioners
        = practitionerRepository.findMedicalPractitionerByPatientsId(patientId);
    return new ResponseEntity<>(practitioners, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/practitioner/patient")
  public ResponseEntity<?> addPatientToPractitioner(
      @RequestBody PractitionerPatientBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioner/patient")
  public ResponseEntity<?> removePatientFromPractitioner(
      @RequestBody PractitionerPatientBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/practitioner/appointments")
  public ResponseEntity<?> getAllAppointmentsByPractitionerId(
      @RequestBody PractitionerAppointmentBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!practitionerRepository.existsById(practitionerId)) {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Appointment> appointments
        = appointmentRepository.findAppointmentsByPractitionerId(practitionerId);
    return new ResponseEntity<>(appointments, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/appointment/practitioner")
  public ResponseEntity<?> getPractitionerByAppointmentId(
      @RequestBody PractitionerAppointmentBody requestBody) {
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

    MedicalPractitioner practitioner
        = practitionerRepository.findMedicalPractitionerByAppointmentsId(appointmentId);
    return new ResponseEntity<>(practitioner, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/practitioner/appointment")
  public ResponseEntity<?> addAppointmentToPractitioner(
      @RequestBody PractitionerAppointmentBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();

      Optional<Appointment> appointmentData = appointmentRepository.findById(appointmentId);

      if (appointmentData.isPresent()) {
        Appointment appointment = appointmentData.get();

        MedicalPractitioner oldPractitioner
            = practitionerRepository.findMedicalPractitionerByAppointmentsId(appointmentId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioner/appointment")
  public ResponseEntity<?> removeAppointmentFromPractitioner(
      @RequestBody PractitionerAppointmentBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/practitioner/prescriptions")
  public ResponseEntity<?> getAllPrescriptionsByPractitionerId(
      @RequestBody PractitionerPrescriptionBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!practitionerRepository.existsById(practitionerId)) {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Prescription> prescriptions
        = prescriptionRepository.findPrescriptionsByPractitionerId(practitionerId);
    return new ResponseEntity<>(prescriptions, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/prescription/practitioner")
  public ResponseEntity<?> getPractitionerByPrescriptionId(
      @RequestBody PractitionerPrescriptionBody requestBody) {
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

    MedicalPractitioner practitioner
        = practitionerRepository.findMedicalPractitionerByPrescriptionsId(prescriptionId);
    return new ResponseEntity<>(practitioner, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/practitioner/prescription")
  public ResponseEntity<?> addPrescriptionToPractitioner(
      @RequestBody PractitionerPrescriptionBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPrescriptionId() == null) {
      String errorMessage = "Missing 'prescriptionId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID prescriptionId = parseUuidFromString(requestBody.getPrescriptionId());
    if (prescriptionId == null) {
      String errorMessage = "'prescriptionId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();

      Optional<Prescription> prescriptionData = prescriptionRepository.findById(prescriptionId);

      if (prescriptionData.isPresent()) {
        Prescription prescription = prescriptionData.get();

        MedicalPractitioner oldPractitioner
            = practitionerRepository.findMedicalPractitionerByPrescriptionsId(prescriptionId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioner/prescription")
  public ResponseEntity<?> removePrescriptionFromPractitioner(
      @RequestBody PractitionerPrescriptionBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPrescriptionId() == null) {
      String errorMessage = "Missing 'prescriptionId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID prescriptionId = parseUuidFromString(requestBody.getPrescriptionId());
    if (prescriptionId == null) {
      String errorMessage = "'prescriptionId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

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
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/practitioner/diagnoses")
  public ResponseEntity<?> getAllDiagnosesByPractitionerId(
      @RequestBody PractitionerDiagnosisBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!practitionerRepository.existsById(practitionerId)) {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByPractitionerId(practitionerId);
    return new ResponseEntity<>(diagnoses, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/diagnosis/practitioner")
  public ResponseEntity<?> getPractitionerByDiagnosisId(
      @RequestBody PractitionerDiagnosisBody requestBody) {
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

    MedicalPractitioner practitioner
        = practitionerRepository.findMedicalPractitionerByDiagnosesId(diagnosisId);
    return new ResponseEntity<>(practitioner, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/practitioner/diagnosis")
  public ResponseEntity<?> addDiagnosisToPractitioner(
      @RequestBody PractitionerDiagnosisBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDiagnosisId() == null) {
      String errorMessage = "Missing 'diagnosisId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID diagnosisId = parseUuidFromString(requestBody.getDiagnosisId());
    if (diagnosisId == null) {
      String errorMessage = "'diagnosisId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();

      Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(diagnosisId);

      if (diagnosisData.isPresent()) {
        Diagnosis diagnosis = diagnosisData.get();

        MedicalPractitioner oldPractitioner
            = practitionerRepository.findMedicalPractitionerByDiagnosesId(diagnosisId);

        if (oldPractitioner != null) {
          oldPractitioner.removeDiagnosisById(diagnosisId);
          practitionerRepository.save(oldPractitioner);

          practitioner.addDiagnosis(diagnosis);
          practitionerRepository.save(practitioner);
          return new ResponseEntity<>(diagnosis, HttpStatus.OK);
        } else {
          practitioner.addDiagnosis(diagnosis);
          practitionerRepository.save(practitioner);
          return new ResponseEntity<>(diagnosis, HttpStatus.OK);
        }
      } else {
        String errorMessage = "No diagnosis found with id " + diagnosisId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/practitioner/diagnosis")
  public ResponseEntity<?> removeDiagnosisFromPractitioner(
      @RequestBody PractitionerDiagnosisBody requestBody) {
    if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDiagnosisId() == null) {
      String errorMessage = "Missing 'diagnosisId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID diagnosisId = parseUuidFromString(requestBody.getDiagnosisId());
    if (diagnosisId == null) {
      String errorMessage = "'diagnosisId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<MedicalPractitioner> practitionerData
        = practitionerRepository.findById(practitionerId);

    if (practitionerData.isPresent()) {
      MedicalPractitioner practitioner = practitionerData.get();

      if (diagnosisRepository.existsById(diagnosisId)) {
        practitioner.removeDiagnosisById(diagnosisId);
        practitionerRepository.save(practitioner);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        String errorMessage = "No diagnosis found with id " + diagnosisId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No medical practitioner found with id " + practitionerId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }
}
