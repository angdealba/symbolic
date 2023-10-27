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
   * Implements GET endpoint /facilities for returning all data.
   */
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
  @GetMapping("/facility")
  public ResponseEntity<?> getFacilityById(@RequestParam("id") Long id) {
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
  @PostMapping("/facility")
  public ResponseEntity<?> createFacility(@RequestBody Facility facility) {
    Facility newFacility = new Facility(
        facility.getLatitude(), facility.getLongitude(), facility.getSpecialization()
    );
    facilityRepository.save(newFacility);
    return new ResponseEntity<>(newFacility, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /facility for updating data matching an id.
   */
  @PutMapping("/facility")
  public ResponseEntity<?> updateFacility(@RequestParam("id") Long id,
                                          @RequestBody Facility facility) {
    Optional<Facility> facilityData = facilityRepository.findById(id);

    if (facilityData.isPresent()) {
      Facility oldFacility = facilityData.get();
      oldFacility.setLatitude(facility.getLatitude());
      oldFacility.setLongitude(facility.getLongitude());
      oldFacility.setSpecialization(facility.getSpecialization());
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
  @DeleteMapping("/facility")
  public ResponseEntity<?> deleteFacility(@RequestParam("id") Long id) {
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
  @DeleteMapping("/facilities")
  public ResponseEntity<?> deleteAllFacilities() {
    List<Facility> facilities = facilityRepository.findAll();

    for (Facility facility : facilities) {
      Long id = facility.getId();

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
  @GetMapping("/facility/patients")
  public ResponseEntity<?> getAllPatientsByFacilityId(@RequestParam("facilityId") Long facilityId) {
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
  @GetMapping("/facility/practitioners")
  public ResponseEntity<?> getAllPractitionersByFacilityId(
      @RequestParam("facilityId") Long facilityId) {
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
  @GetMapping("/patient/facilities")
  public ResponseEntity<?> getAllFacilitiesByPatientId(@RequestParam("patientId") Long patientId) {
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
  @GetMapping("/practitioner/facility")
  public ResponseEntity<?> getFacilityByPractitionerId(
      @RequestParam("practitionerId") Long practitionerId) {
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
  @PostMapping("/facility/patient")
  public ResponseEntity<?> addPatientToFacility(@RequestParam("facilityId") Long facilityId,
                                                @RequestParam("patientId") Long patientId) {
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
  @DeleteMapping("/facility/patient")
  public ResponseEntity<?> removePatientFromFacility(@RequestParam("facilityId") Long facilityId,
                                                     @RequestParam("patientId") Long patientId) {
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
  @PostMapping("/facility/practitioner")
  public ResponseEntity<?> addPractitionerToFacility(
      @RequestParam("facilityId") Long facilityId,
      @RequestParam("practitionerId") Long practitionerId) {
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
  @DeleteMapping("/facility/practitioner")
  public ResponseEntity<?> removePractitionerFromFacility(
      @RequestParam("facilityId") Long facilityId,
      @RequestParam("practitionerId") Long practitionerId) {
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
  @GetMapping("/facility/appointments")
  public ResponseEntity<?> getAllAppointmentsByFacilityId(
      @RequestParam("facilityId") Long facilityId) {
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
  @GetMapping("/appointment/facility")
  public ResponseEntity<?> getFacilityByAppointmentId(
      @RequestParam("appointmentId") Long appointmentId) {
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
      @RequestParam("facilityId") Long facilityId,
      @RequestParam("appointmentId") Long appointmentId) {
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
  @DeleteMapping("/facility/appointment")
  public ResponseEntity<?> removeAppointmentFromFacility(
      @RequestParam("facilityId") Long facilityId,
      @RequestParam("appointmentId") Long appointmentId) {
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
