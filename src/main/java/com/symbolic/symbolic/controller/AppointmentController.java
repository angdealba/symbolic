package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Appointment;
import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.AppointmentRepository;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
 * Implements all functionality for the appointment data API.
 */
@RestController
@Secured("ROLE_ADMIN")
@RequestMapping("/api")
public class AppointmentController {
  @Autowired
  AppointmentRepository appointmentRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;
  @Autowired
  FacilityRepository facilityRepository;

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  /**
   * RequestBody object used to represent Appointment-related requests.
   */
  static class AppointmentRequestBody {
    String id;
    String dateTime;
    Integer cost;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getDateTime() {
      return dateTime;
    }

    public Integer getCost() {
      return cost;
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
   * Implements GET endpoint /appointments for returning all data.
   */
  @GetMapping("/appointments")
  public ResponseEntity<?> getAllAppointments() {
    List<Appointment> appointments = new ArrayList<>();
    appointments.addAll(appointmentRepository.findAll());

    if (appointments.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(appointments, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /appointment for returning data matching an id.
   */
  @GetMapping("/appointment")
  public ResponseEntity<?> getAppointmentById(@RequestBody AppointmentRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Appointment> appointmentData = appointmentRepository.findById(id);

    if (appointmentData.isPresent()) {
      Appointment appointment = appointmentData.get();
      return new ResponseEntity<>(appointment, HttpStatus.OK);
    } else {
      String errorMessage = "No appointment found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /appointment for uploading data.
   */
  @PostMapping("/appointment")
  public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequestBody requestBody) {
    if (requestBody.getDateTime() == null) {
      String errorMessage = "Missing 'dateTime' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getCost() == null) {
      String errorMessage = "Missing 'cost' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getCost() < 0) {
      String errorMessage = "'cost' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    // Parse the date and return an error message if it is not in the yyyy-MM-dd HH:mm format
    Date parsedDateTime;
    try {
      parsedDateTime = formatter.parse(requestBody.getDateTime());
    } catch (ParseException e) {
      String errorMessage = "'dateTime' field value must be in the format yyyy-MM-dd HH:mm";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Appointment newAppointment = new Appointment(
        parsedDateTime, requestBody.getCost()
    );

    appointmentRepository.save(newAppointment);
    return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /appointment for updating data matching an id.
   */
  @PutMapping("/appointment")
  public ResponseEntity<?> updateAppointment(@RequestBody AppointmentRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Appointment> appointmentData = appointmentRepository.findById(id);

    if (appointmentData.isPresent()) {
      Appointment oldAppointment = appointmentData.get();

      if (requestBody.getDateTime() != null) {
        // Parse the date and return an error message if it is not in the yyyy-MM-dd HH:mm format
        try {
          Date parsedDateTime = formatter.parse(requestBody.getDateTime());
          oldAppointment.setDateTime(parsedDateTime);
        } catch (ParseException e) {
          String errorMessage = "'dateTime' field value must be in the format yyyy-MM-dd HH:mm";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
      }

      if (requestBody.getCost() != null) {
        if (requestBody.getCost() < 0) {
          String errorMessage = "'cost' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldAppointment.setCost(requestBody.getCost());
        }
      }

      appointmentRepository.save(oldAppointment);
      return new ResponseEntity<>(oldAppointment, HttpStatus.OK);
    } else {
      String errorMessage = "No appointment found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /appointment for removing data matching an id.
   */
  @DeleteMapping("/appointment")
  public ResponseEntity<?> deleteAppointment(@RequestBody AppointmentRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Appointment> appointmentData = appointmentRepository.findById(id);

    if (appointmentData.isPresent()) {
      Appointment appointment = appointmentData.get();

      Patient patient = appointment.getPatient();
      if (patient != null) {
        patient.removeAppointmentById(id);
        patientRepository.save(patient);
      }

      MedicalPractitioner practitioner = appointment.getPractitioner();
      if (practitioner != null) {
        practitioner.removeAppointmentById(id);
        practitionerRepository.save(practitioner);
      }

      Facility facility = appointment.getFacility();
      if (facility != null) {
        facility.removeAppointmentById(id);
        facilityRepository.save(facility);
      }

      appointmentRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No appointment found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /appointments for removing all data.
   */
  @DeleteMapping("/appointments")
  public ResponseEntity<?> deleteAllAppointments() {
    List<Appointment> appointments = appointmentRepository.findAll();

    for (Appointment appointment : appointments) {
      UUID id = appointment.getId();

      Patient patient = appointment.getPatient();
      if (patient != null) {
        patient.removeAppointmentById(id);
        patientRepository.save(patient);
      }

      MedicalPractitioner practitioner = appointment.getPractitioner();
      if (practitioner != null) {
        practitioner.removeAppointmentById(id);
        practitionerRepository.save(practitioner);
      }

      Facility facility = appointment.getFacility();
      if (facility != null) {
        facility.removeAppointmentById(id);
        appointmentRepository.save(appointment);
      }
    }

    appointmentRepository.deleteAll();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
