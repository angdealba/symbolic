package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.DiagnosisRepository;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the diagnosis data API.
 */
@RestController
@RequestMapping("/api")
public class DiagnosisController {
  @Autowired
  DiagnosisRepository diagnosisRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

  /**
   * RequestBody object used to represent Diagnosis-related requests.
   */
  static class DiagnosisRequestBody {
    String id;
    String condition;
    String treatmentInfo;
    String date;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getCondition() {
      return condition;
    }

    public String getTreatmentInfo() {
      return treatmentInfo;
    }

    public String getDate() {
      return date;
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
   * Implements GET endpoint /diagnoses for returning all data.
   */
  @GetMapping("/diagnoses")
  public ResponseEntity<?> getAllDiagnoses() {
    List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.addAll(diagnosisRepository.findAll());

    if (diagnoses.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(diagnoses, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /diagnosis for returning data matching an id.
   */
  @GetMapping("/diagnosis")
  public ResponseEntity<?> getDiagnosisById(@RequestBody DiagnosisRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

    if (diagnosisData.isPresent()) {
      Diagnosis diagnosis = diagnosisData.get();
      return new ResponseEntity<>(diagnosis, HttpStatus.OK);
    } else {
      String errorMessage = "No diagnosis found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /diagnosis for uploading data.
   */
  @PostMapping("/diagnosis")
  public ResponseEntity<?> createDiagnosis(@RequestBody DiagnosisRequestBody requestBody) {
    if (requestBody.getCondition() == null) {
      String errorMessage = "Missing 'condition' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getTreatmentInfo() == null) {
      String errorMessage = "Missing 'treatmentInfo' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getDate() == null) {
      String errorMessage = "Missing 'date' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    // Attempt to parse the date and return an error message if it is not in the yyyy-MM-dd format
    Date parsedDate;
    try {
      parsedDate = formatter.parse(requestBody.getDate());
    } catch (ParseException e) {
      String errorMessage = "'date' field value must be in the format yyyy-MM-dd";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Diagnosis newDiagnosis = new Diagnosis(
        requestBody.getCondition(), requestBody.getTreatmentInfo(), parsedDate
    );

    diagnosisRepository.save(newDiagnosis);
    return new ResponseEntity<>(newDiagnosis, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /diagnosis for updating data matching an id.
   */
  @PutMapping("/diagnosis")
  public ResponseEntity<?> updateDiagnosis(@RequestBody DiagnosisRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

    if (diagnosisData.isPresent()) {
      Diagnosis oldDiagnosis = diagnosisData.get();

      if (requestBody.getCondition() != null) {
        oldDiagnosis.setCondition(requestBody.getCondition());
      }

      if (requestBody.getTreatmentInfo() != null) {
        oldDiagnosis.setTreatmentInfo(requestBody.getTreatmentInfo());
      }

      if (requestBody.getDate() != null) {
        try {
          Date parsedDate = formatter.parse(requestBody.getDate());
          oldDiagnosis.setDate(parsedDate);
        } catch (ParseException e) {
          String errorMessage = "'date' field value must be in the format yyyy-MM-dd";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }
      }

      diagnosisRepository.save(oldDiagnosis);
      return new ResponseEntity<>(oldDiagnosis, HttpStatus.OK);
    } else {
      String errorMessage = "No diagnosis found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /diagnosis for removing data matching an id.
   */
  @DeleteMapping("/diagnosis")
  public ResponseEntity<?> deleteDiagnosis(@RequestBody DiagnosisRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

    if (diagnosisData.isPresent()) {
      Diagnosis diagnosis = diagnosisData.get();

      Patient patient = diagnosis.getPatient();
      if (patient != null) {
        patient.removeDiagnosisById(id);
        patientRepository.save(patient);
      }

      MedicalPractitioner practitioner = diagnosis.getPractitioner();
      if (practitioner != null) {
        practitioner.removeDiagnosisById(id);
        practitionerRepository.save(practitioner);
      }

      diagnosisRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No diagnosis found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /diagnoses for removing all data.
   */
  @DeleteMapping("/diagnoses")
  public ResponseEntity<?> deleteAllDiagnoses() {
    List<Diagnosis> diagnoses = diagnosisRepository.findAll();

    for (Diagnosis diagnosis : diagnoses) {
      UUID id = diagnosis.getId();

      Patient patient = diagnosis.getPatient();
      if (patient != null) {
        patient.removeDiagnosisById(id);
        patientRepository.save(patient);
      }

      MedicalPractitioner practitioner = diagnosis.getPractitioner();
      if (practitioner != null) {
        practitioner.removeDiagnosisById(id);
        practitionerRepository.save(practitioner);
      }
    }

    diagnosisRepository.deleteAll();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }
}
