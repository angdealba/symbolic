package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import com.symbolic.symbolic.repository.PrescriptionRepository;
import java.util.ArrayList;
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
 * Implements all functionality for the prescription data API.
 */
@RestController
@Secured("ROLE_ADMIN")
@RequestMapping("/api")
public class PrescriptionController {
  @Autowired
  PrescriptionRepository prescriptionRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;

  /**
   * RequestBody object used to represent Prescription-related requests.
   */
  static class PrescriptionRequestBody {
    String id;
    Integer dosage;
    Integer dailyUses;
    Integer cost;
    String instructions;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Integer getDosage() {
      return dosage;
    }

    public Integer getDailyUses() {
      return dailyUses;
    }

    public Integer getCost() {
      return cost;
    }

    public String getInstructions() {
      return instructions;
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
   * Implements GET endpoint /prescriptions for returning all data.
   */
  @GetMapping("/prescriptions")
  public ResponseEntity<?> getAllPrescriptions() {
    List<Prescription> prescriptions = new ArrayList<>();
    prescriptions.addAll(prescriptionRepository.findAll());

    if (prescriptions.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(prescriptions, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /prescription for returning data matching an id.
   */
  @GetMapping("/prescription")
  public ResponseEntity<?> getPrescriptionById(@RequestBody PrescriptionRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

    if (prescriptionData.isPresent()) {
      Prescription prescription = prescriptionData.get();
      return new ResponseEntity<>(prescription, HttpStatus.OK);
    } else {
      String errorMessage = "No prescription found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /prescription for uploading data.
   */
  @PostMapping("/prescription")
  public ResponseEntity<?> createPrescription(@RequestBody PrescriptionRequestBody requestBody) {
    if (requestBody.getDosage() == null) {
      String errorMessage = "Missing 'dosage' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDailyUses() == null) {
      String errorMessage = "Missing 'dailyUses' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getCost() == null) {
      String errorMessage = "Missing 'cost' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDosage() < 0) {
      String errorMessage = "'dosage' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDailyUses() < 0) {
      String errorMessage = "'dailyUses' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getCost() < 0) {
      String errorMessage = "'cost' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Prescription newPrescription = new Prescription(
        requestBody.getDosage(), requestBody.getDailyUses(), requestBody.getCost(),
        requestBody.getInstructions()
    );

    prescriptionRepository.save(newPrescription);
    return new ResponseEntity<>(newPrescription, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /prescription for updating data matching an id.
   */
  @PutMapping("/prescription")
  public ResponseEntity<?> updatePrescription(@RequestBody PrescriptionRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

    if (prescriptionData.isPresent()) {
      Prescription oldPrescription = prescriptionData.get();

      if (requestBody.getDosage() != null) {
        if (requestBody.getDosage() < 0) {
          String errorMessage = "'dosage' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPrescription.setDosage(requestBody.getDosage());
        }
      }

      if (requestBody.getDailyUses() != null) {
        if (requestBody.getDailyUses() < 0) {
          String errorMessage = "'dailyUses' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPrescription.setDailyUses(requestBody.getDailyUses());
        }
      }

      if (requestBody.getCost() != null) {
        if (requestBody.getCost() < 0) {
          String errorMessage = "'cost' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPrescription.setCost(requestBody.getCost());
        }
      }

      if (requestBody.getInstructions() != null) {
        oldPrescription.setInstructions(requestBody.getInstructions());
      }

      prescriptionRepository.save(oldPrescription);
      return new ResponseEntity<>(oldPrescription, HttpStatus.OK);
    } else {
      String errorMessage = "No prescription found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /prescription for removing data matching an id.
   */
  @DeleteMapping("/prescription")
  public ResponseEntity<?> deletePrescription(@RequestBody PrescriptionRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Prescription> prescriptionData = prescriptionRepository.findById(id);

    if (prescriptionData.isPresent()) {
      Prescription prescription = prescriptionData.get();

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

      prescriptionRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No prescription found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /prescriptions for removing all data.
   */
  @DeleteMapping("/prescriptions")
  public ResponseEntity<?> deleteAllPrescriptions() {
    List<Prescription> prescriptions = prescriptionRepository.findAll();

    for (Prescription prescription : prescriptions) {
      UUID id = prescription.getId();

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
