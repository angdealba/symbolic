package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Appointment;
import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.AppointmentRepository;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
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
 * Implements all functionality for the facility data API.
 */
@RestController
@RequestMapping("/api")
public class FacilityController {
  @Autowired
  FacilityRepository facilityRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  AppointmentRepository appointmentRepository;

  /**
   * RequestBody object used to represent Facility-related requests.
   */
  static class FacilityRequestBody {
    String id;
    Double latitude;
    Double longitude;
    String specialization;

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
  }

  /**
   * RequestBody object used to represent Facility-Patient join requests.
   */
  static class FacilityPatientBody {
    String facilityId;
    String patientId;

    public String getFacilityId() {
      return facilityId;
    }

    public String getPatientId() {
      return patientId;
    }
  }

  /**
   * RequestBody object used to represent Facility-Practitioner join requests.
   */
  static class FacilityPractitionerBody {
    String facilityId;
    String practitionerId;

    public String getFacilityId() {
      return facilityId;
    }

    public String getPractitionerId() {
      return practitionerId;
    }
  }

  /**
   * RequestBody object used to represent Facility-Appointment join requests.
   */
  static class FacilityAppointmentBody {
    String facilityId;
    String appointmentId;

    public String getFacilityId() {
      return facilityId;
    }

    public String getAppointmentId() {
      return appointmentId;
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
   * Implements GET endpoint /facilities for returning all data.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/facilities")
  public ResponseEntity<?> getAllFacilities() {
    List<Facility> facilities = new ArrayList<>();
    facilities.addAll(facilityRepository.findAll());

    if (facilities.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(facilities, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /facility for returning data matching an id.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/facility")
  public ResponseEntity<?> getFacilityById(@RequestBody FacilityRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(id);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();
      return new ResponseEntity<>(facility, HttpStatus.OK);
    } else {
      String errorMessage = "No facility found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /facility for uploading data.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/facility")
  public ResponseEntity<?> createFacility(@RequestBody FacilityRequestBody requestBody) {
    if (requestBody.getLatitude() == null) {
      String errorMessage = "Missing 'latitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getLongitude() == null) {
      String errorMessage = "Missing 'longitude' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getSpecialization() == null) {
      String errorMessage = "Missing 'specialization' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Facility newFacility = new Facility(
        requestBody.getLatitude(), requestBody.getLongitude(), requestBody.getSpecialization()
    );
    facilityRepository.save(newFacility);
    return new ResponseEntity<>(newFacility, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /facility for updating data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @PutMapping("/facility")
  public ResponseEntity<?> updateFacility(@RequestBody FacilityRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(id);

    if (facilityData.isPresent()) {
      Facility oldFacility = facilityData.get();

      if (requestBody.getLatitude() != null) {
        oldFacility.setLatitude(requestBody.getLatitude());
      }

      if (requestBody.getLongitude() != null) {
        oldFacility.setLongitude(requestBody.getLongitude());
      }

      if (requestBody.getSpecialization() != null) {
        oldFacility.setSpecialization(requestBody.getSpecialization());
      }

      facilityRepository.save(oldFacility);
      return new ResponseEntity<>(oldFacility, HttpStatus.OK);
    } else {
      String errorMessage = "No facility found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /facility for removing data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/facility")
  public ResponseEntity<?> deleteFacility(@RequestBody FacilityRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(id);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      Set<Patient> patients = facility.getPatients();
      for (Patient patient : patients) {
        patient.removeFacilityById(id);
        patientRepository.save(patient);
      }

      Set<MedicalPractitioner> practitioners = facility.getPractitioners();
      for (MedicalPractitioner practitioner : practitioners) {
        practitioner.setFacility(null);
        practitionerRepository.save(practitioner);
      }

      Set<Appointment> appointments = facility.getAppointments();
      for (Appointment appointment : appointments) {
        appointment.setFacility(null);
        appointmentRepository.save(appointment);
      }

      facilityRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No facility found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /facilities for removing all data.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/facilities")
  public ResponseEntity<?> deleteAllFacilities() {
    List<Facility> facilities = facilityRepository.findAll();

    for (Facility facility : facilities) {
      UUID id = facility.getId();

      Set<Patient> patients = facility.getPatients();
      for (Patient patient : patients) {
        patient.removeFacilityById(id);
        patientRepository.save(patient);
      }

      Set<MedicalPractitioner> practitioners = facility.getPractitioners();
      for (MedicalPractitioner practitioner : practitioners) {
        practitioner.setFacility(null);
        practitionerRepository.save(practitioner);
      }

      Set<Appointment> appointments = facility.getAppointments();
      for (Appointment appointment : appointments) {
        appointment.setFacility(null);
        appointmentRepository.save(appointment);
      }
    }

    facilityRepository.deleteAll();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Implements GET endpoint /facility/patients for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/facility/patients")
  public ResponseEntity<?> getAllPatientsByFacilityId(
      @RequestBody FacilityPatientBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!facilityRepository.existsById(facilityId)) {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Patient> patients = patientRepository.findPatientsByFacilitiesId(facilityId);
    return new ResponseEntity<>(patients, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /facility/practitioners for returning data matching an id.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/facility/practitioners")
  public ResponseEntity<?> getAllPractitionersByFacilityId(
      @RequestBody FacilityPractitionerBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!facilityRepository.existsById(facilityId)) {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<MedicalPractitioner> practitioners = practitionerRepository
        .findMedicalPractitionerByFacilityId(facilityId);
    return new ResponseEntity<>(practitioners, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /patient/facilties for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/patient/facilities")
  public ResponseEntity<?> getAllFacilitiesByPatientId(
      @RequestBody FacilityPatientBody requestBody) {
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

    List<Facility> facilities = facilityRepository.findFacilitiesByPatientsId(patientId);
    return new ResponseEntity<>(facilities, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /practitioner/facility for returning data matching an id.
   */
  @Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
  @GetMapping("/practitioner/facility")
  public ResponseEntity<?> getFacilityByPractitionerId(
      @RequestBody FacilityPractitionerBody requestBody) {
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

    Facility facility = facilityRepository.findFacilityByPractitionersId(practitionerId);
    return new ResponseEntity<>(facility, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/facility/patient")
  public ResponseEntity<?> addPatientToFacility(@RequestBody FacilityPatientBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      Optional<Patient> patientData = patientRepository.findById(patientId);

      if (patientData.isPresent()) {
        Patient patient = patientData.get();

        facility.addPatient(patient);
        facilityRepository.save(facility);
        return new ResponseEntity<>(patient, HttpStatus.OK);
      } else {
        String errorMessage = "No patient found with id " + patientId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/facility/patient")
  public ResponseEntity<?> removePatientFromFacility(@RequestBody FacilityPatientBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      if (patientRepository.existsById(patientId)) {
        facility.removePatientById(patientId);
        facilityRepository.save(facility);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        String errorMessage = "No patient found with id " + patientId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @Secured("ROLE_ADMIN")
  @PostMapping("/facility/practitioner")
  public ResponseEntity<?> addPractitionerToFacility(
      @RequestBody FacilityPractitionerBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      Optional<MedicalPractitioner> practitionerData
          = practitionerRepository.findById(practitionerId);

      if (practitionerData.isPresent()) {
        MedicalPractitioner practitioner = practitionerData.get();

        Facility oldFacility = facilityRepository.findFacilityByPractitionersId(practitionerId);

        if (oldFacility != null) {
          oldFacility.removePractitionerById(practitionerId);
          facilityRepository.save(oldFacility);
          facility.addPractitioner(practitioner);
          facilityRepository.save(facility);
          return new ResponseEntity<>(practitioner, HttpStatus.OK);
        } else {
          facility.addPractitioner(practitioner);
          facilityRepository.save(facility);
          return new ResponseEntity<>(practitioner, HttpStatus.OK);
        }
      } else {
        String errorMessage = "No medical practitioner found with id " + practitionerId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/facility/practitioner")
  public ResponseEntity<?> removePractitionerFromFacility(
      @RequestBody FacilityPractitionerBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPractitionerId() == null) {
      String errorMessage = "Missing 'practitionerId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID practitionerId = parseUuidFromString(requestBody.getPractitionerId());
    if (practitionerId == null) {
      String errorMessage = "'practitionerId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      if (practitionerRepository.existsById(practitionerId)) {
        facility.removePractitionerById(practitionerId);
        facilityRepository.save(facility);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        String errorMessage = "No medical practitioner found with id " + practitionerId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/facility/appointments")
  public ResponseEntity<?> getAllAppointmentsByFacilityId(
      @RequestBody FacilityAppointmentBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!facilityRepository.existsById(facilityId)) {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Appointment> appointments = appointmentRepository.findAppointmentsByFacilityId(facilityId);
    return new ResponseEntity<>(appointments, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint for returning data matching an id.
   */
  @Secured("ROLE_ADMIN")
  @GetMapping("/appointment/facility")
  public ResponseEntity<?> getFacilityByAppointmentId(
      @RequestBody FacilityAppointmentBody requestBody) {
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

    Facility facility = facilityRepository.findFacilityByAppointmentsId(appointmentId);
    return new ResponseEntity<>(facility, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @PostMapping("/facility/appointment")
  public ResponseEntity<?> addAppointmentToFacility(
      @RequestBody FacilityAppointmentBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      Optional<Appointment> appointmentData = appointmentRepository.findById(appointmentId);

      if (appointmentData.isPresent()) {
        Appointment appointment = appointmentData.get();

        Facility oldFacility = facilityRepository.findFacilityByAppointmentsId(appointmentId);

        if (oldFacility != null) {
          oldFacility.removeAppointmentById(appointmentId);
          facilityRepository.save(oldFacility);

          facility.addAppointment(appointment);
          facilityRepository.save(facility);
          return new ResponseEntity<>(appointment, HttpStatus.OK);
        } else {
          facility.addAppointment(appointment);
          facilityRepository.save(facility);
          return new ResponseEntity<>(appointment, HttpStatus.OK);
        }
      } else {
        String errorMessage = "No appointment found with id " + appointmentId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @Secured("ROLE_ADMIN")
  @DeleteMapping("/facility/appointment")
  public ResponseEntity<?> removeAppointmentFromFacility(
      @RequestBody FacilityAppointmentBody requestBody) {
    if (requestBody.getFacilityId() == null) {
      String errorMessage = "Missing 'facilityId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getAppointmentId() == null) {
      String errorMessage = "Missing 'appointmentId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID facilityId = parseUuidFromString(requestBody.getFacilityId());
    if (facilityId == null) {
      String errorMessage = "'facilityId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID appointmentId = parseUuidFromString(requestBody.getAppointmentId());
    if (appointmentId == null) {
      String errorMessage = "'appointmentId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Facility> facilityData = facilityRepository.findById(facilityId);

    if (facilityData.isPresent()) {
      Facility facility = facilityData.get();

      if (appointmentRepository.existsById(appointmentId)) {
        facility.removeAppointmentById(appointmentId);
        facilityRepository.save(facility);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        String errorMessage = "No appointment found with id " + appointmentId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No facility found with id " + facilityId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }
}
