package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.InsurancePolicy;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.InsurancePolicyRepository;
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
 * Implements all functionality for the insurance policy data API.
 */
@RestController
@RequestMapping("/api")
public class InsurancePolicyController {
  @Autowired
  InsurancePolicyRepository insurancePolicyRepository;
  @Autowired
  PatientRepository patientRepository;

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
  public ResponseEntity<?> getPolicyById(@RequestParam("id") Long id) {
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
  public ResponseEntity<?> createPolicy(@RequestBody InsurancePolicy policy) {
    InsurancePolicy newPolicy = new InsurancePolicy(policy.getPremiumCost());
    insurancePolicyRepository.save(newPolicy);
    return new ResponseEntity<>(newPolicy, HttpStatus.CREATED);
  }

  /**
   * Implements PUT endpoint /policy for updating data matching an id.
   */
  @PutMapping("/policy")
  public ResponseEntity<?> updatePolicy(@RequestParam("id") Long id,
                                        @RequestBody InsurancePolicy policy) {
    Optional<InsurancePolicy> policyData = insurancePolicyRepository.findById(id);

    if (policyData.isPresent()) {
      InsurancePolicy oldPolicy = policyData.get();
      oldPolicy.setPremiumCost(policy.getPremiumCost());
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
  public ResponseEntity<?> deletePolicy(@RequestParam("id") Long id) {
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
  public ResponseEntity<?> getAllPatientsByPolicyId(@RequestParam("policyId") Long policyId) {
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
  public ResponseEntity<?> getPolicyByPatientId(@RequestParam("patientId") Long patientId) {
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
  public ResponseEntity<?> addPatientToPolicy(@RequestParam("policyId") Long policyId,
                                              @RequestParam("patientId") Long patientId) {
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
  public ResponseEntity<?> removePatientFromPolicy(@RequestParam("policyId") Long policyId,
                                                   @RequestParam("patientId") Long patientId) {
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
