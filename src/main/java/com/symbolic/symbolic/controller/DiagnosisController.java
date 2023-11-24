package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.DiagnosisRepository;
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
 * RequestBody object used to represent Diagnosis-related requests
 */
class DiagnosisRequestBody {
  Long id;
  Diagnosis diagnosis;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Diagnosis getDiagnosis() {
    return diagnosis;
  }

  public void setDiagnosis(Diagnosis diagnosis) {
    this.diagnosis = diagnosis;
  }
}

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
    Long id = requestBody.getId();

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
    if (requestBody.getDiagnosis() == null) {
      String errorMessage = "Missing 'diagnosis' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    Diagnosis diagnosis = requestBody.getDiagnosis();

    Diagnosis newDiagnosis = new Diagnosis(
        diagnosis.getCondition(), diagnosis.getTreatmentInfo(), diagnosis.getDate()
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
    Long id = requestBody.getId();

    if (requestBody.getDiagnosis() == null) {
      String errorMessage = "Missing 'diagnosis' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    Diagnosis diagnosis = requestBody.getDiagnosis();

    Optional<Diagnosis> diagnosisData = diagnosisRepository.findById(id);

    if (diagnosisData.isPresent()) {
      Diagnosis oldDiagnosis = diagnosisData.get();
      oldDiagnosis.setCondition(diagnosis.getCondition());
      oldDiagnosis.setTreatmentInfo(diagnosis.getTreatmentInfo());
      oldDiagnosis.setDate(diagnosis.getDate());
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
    Long id = requestBody.getId();

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
      Long id = diagnosis.getId();

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
