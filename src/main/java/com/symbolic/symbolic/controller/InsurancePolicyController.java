package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.InsurancePolicy;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.InsurancePolicyRepository;
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
 * Implements all functionality for the insurance policy data API.
 */
@RestController
@Secured("ROLE_ADMIN")
@RequestMapping("/api")
public class InsurancePolicyController {
  @Autowired
  InsurancePolicyRepository insurancePolicyRepository;
  @Autowired
  PatientRepository patientRepository;

  /**
   * RequestBody object used to represent InsurancePolicy-related requests.
   */
  static class PolicyRequestBody {
    String id;
    Integer premiumCost;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public Integer getPremiumCost() {
      return premiumCost;
    }
  }

  /**
   * RequestBody object used to represent Policy-Patient join requests.
   */
  static class PolicyPatientBody {
    String policyId;
    String patientId;

    public String getPolicyId() {
      return policyId;
    }

    public String getPatientId() {
      return patientId;
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
   * Implements GET endpoint /policies for returning all data.
   */
  @GetMapping("/policies")
  public ResponseEntity<?> getAllPolicies() {
    List<InsurancePolicy> policies = new ArrayList<>();
    policies.addAll(insurancePolicyRepository.findAll());

    if (policies.isEmpty()) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    return new ResponseEntity<>(policies, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /policy for returning data matching an id.
   */
  @GetMapping("/policy")
  public ResponseEntity<?> getPolicyById(@RequestBody PolicyRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(id);

    if (policyData.isPresent()) {
      InsurancePolicy policy = policyData.get();
      return new ResponseEntity<>(policy, HttpStatus.OK);
    } else {
      String errorMessage = "No insurance policy found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements POST endpoint /policy for uploading data.
   */
  @PostMapping("/policy")
  public ResponseEntity<?> createPolicy(@RequestBody PolicyRequestBody requestBody) {
    if (requestBody.getPremiumCost() == null) {
      String errorMessage = "Missing 'premiumCost' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPremiumCost() < 0) {
      String errorMessage = "'premiumCost' field must be a non-negative integer";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    InsurancePolicy newPolicy = new InsurancePolicy(requestBody.getPremiumCost());
    insurancePolicyRepository.save(newPolicy);
    return new ResponseEntity<>(newPolicy, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /policy for updating data matching an id.
   */
  @PutMapping("/policy")
  public ResponseEntity<?> updatePolicy(@RequestBody PolicyRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(id);

    if (policyData.isPresent()) {
      InsurancePolicy oldPolicy = policyData.get();

      if (requestBody.getPremiumCost() != null) {
        if (requestBody.getPremiumCost() < 0) {
          String errorMessage = "'premiumCost' field must be a non-negative integer";
          return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        } else {
          oldPolicy.setPremiumCost(requestBody.getPremiumCost());
        }
      }

      insurancePolicyRepository.save(oldPolicy);
      return new ResponseEntity<>(oldPolicy, HttpStatus.OK);
    } else {
      String errorMessage = "No insurance policy found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /policy for removing data matching an id.
   */
  @DeleteMapping("/policy")
  public ResponseEntity<?> deletePolicy(@RequestBody PolicyRequestBody requestBody) {
    if (requestBody.getId() == null) {
      String errorMessage = "Missing 'id' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID id = parseUuidFromString(requestBody.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(id);

    if (policyData.isPresent()) {
      InsurancePolicy policy = policyData.get();

      Set<Patient> patients = policy.getPatients();
      for (Patient patient : patients) {
        patient.setInsurancePolicy(null);
        patientRepository.save(patient);
      }

      insurancePolicyRepository.deleteById(id);
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    } else {
      String errorMessage = "No insurance policy found with id " + id;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint /policies for removing all data.
   */
  @DeleteMapping("/policies")
  public ResponseEntity<?> deleteAllPolicies() {
    List<InsurancePolicy> policies = insurancePolicyRepository.findAll();

    for (InsurancePolicy policy : policies) {
      Set<Patient> patients = policy.getPatients();
      for (Patient patient : patients) {
        patient.setInsurancePolicy(null);
        patientRepository.save(patient);
      }
    }

    insurancePolicyRepository.deleteAll();
    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
  }

  /**
   * Implements GET endpoint /policy/patients for returning data matching an id.
   */
  @GetMapping("/policy/patients")
  public ResponseEntity<?> getAllPatientsByPolicyId(@RequestBody PolicyPatientBody requestBody) {
    if (requestBody.getPolicyId() == null) {
      String errorMessage = "Missing 'policyId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID policyId = parseUuidFromString(requestBody.getPolicyId());
    if (policyId == null) {
      String errorMessage = "'policyId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    if (!insurancePolicyRepository.existsById(policyId)) {
      String errorMessage = "No insurance policy found with id " + policyId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    List<Patient> patients = patientRepository.findPatientsByInsurancePolicyId(policyId);
    return new ResponseEntity<>(patients, HttpStatus.OK);
  }

  /**
   * Implements GET endpoint /patient/policy for returning data matching an id.
   */
  @GetMapping("/patient/policy")
  public ResponseEntity<?> getPolicyByPatientId(@RequestBody PolicyPatientBody requestBody) {
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

    InsurancePolicy policy = insurancePolicyRepository.findInsurancePolicyByPatientsId(patientId);
    return new ResponseEntity<>(policy, HttpStatus.OK);
  }

  /**
   * Implements POST endpoint for linking the two data types.
   */
  @PostMapping("/policy/patient")
  public ResponseEntity<?> addPatientToPolicy(@RequestBody PolicyPatientBody requestBody) {
    if (requestBody.getPolicyId() == null) {
      String errorMessage = "Missing 'policyId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID policyId = parseUuidFromString(requestBody.getPolicyId());
    if (policyId == null) {
      String errorMessage = "'policyId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(policyId);

    if (policyData.isPresent()) {
      InsurancePolicy policy = policyData.get();

      Optional<Patient> patientData = patientRepository.findById(patientId);

      if (patientData.isPresent()) {
        Patient patient = patientData.get();

        InsurancePolicy oldPolicy = insurancePolicyRepository
            .findInsurancePolicyByPatientsId(patientId);

        if (oldPolicy != null) {
          oldPolicy.removePatientById(patientId);
          insurancePolicyRepository.save(oldPolicy);

          policy.addPatient(patient);
          insurancePolicyRepository.save(policy);
          return new ResponseEntity<>(patient, HttpStatus.OK);
        } else {
          policy.addPatient(patient);
          insurancePolicyRepository.save(policy);
          return new ResponseEntity<>(patient, HttpStatus.OK);
        }
      } else {
        String errorMessage = "No patient found with id " + patientId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No insurance policy found with id " + policyId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }

  /**
   * Implements DELETE endpoint for removing a link between the two data types.
   */
  @DeleteMapping("/policy/patient")
  public ResponseEntity<?> removePatientFromPolicy(@RequestBody PolicyPatientBody requestBody) {
    if (requestBody.getPolicyId() == null) {
      String errorMessage = "Missing 'policyId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (requestBody.getPatientId() == null) {
      String errorMessage = "Missing 'patientId' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID policyId = parseUuidFromString(requestBody.getPolicyId());
    if (policyId == null) {
      String errorMessage = "'policyId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    UUID patientId = parseUuidFromString(requestBody.getPatientId());
    if (patientId == null) {
      String errorMessage = "'patientId' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(policyId);

    if (policyData.isPresent()) {
      InsurancePolicy policy = policyData.get();

      if (patientRepository.existsById(patientId)) {
        policy.removePatientById(patientId);
        insurancePolicyRepository.save(policy);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
      } else {
        String errorMessage = "No patient found with id " + patientId;
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
      }
    } else {
      String errorMessage = "No insurance policy found with id " + policyId;
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }
  }
}
