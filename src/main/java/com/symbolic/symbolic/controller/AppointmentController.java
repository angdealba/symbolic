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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the appointment data API.
 */
@RestController
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
  public ResponseEntity<?> getAppointmentById(@RequestParam("id") Long id) {
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
  public ResponseEntity<?> createAppointment(@RequestBody Appointment appointment) {
    Appointment newAppointment = new Appointment(
        appointment.getDateTime(), appointment.getCost()
    );

    appointmentRepository.save(newAppointment);
    return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /appointment for updating data matching an id.
   */
  @PutMapping("/appointment")
  public ResponseEntity<?> updateAppointment(@RequestParam("id") Long id,
                                             @RequestBody Appointment appointment) {
    Optional<Appointment> appointmentData = appointmentRepository.findById(id);

    if (appointmentData.isPresent()) {
      Appointment oldAppointment = appointmentData.get();
      oldAppointment.setDateTime(appointment.getDateTime());
      oldAppointment.setCost(appointment.getCost());
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
  public ResponseEntity<?> deleteAppointment(@RequestParam("id") Long id) {
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
      Long id = appointment.getId();

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
