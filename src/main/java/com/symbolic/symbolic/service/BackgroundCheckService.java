package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.InsurancePolicyRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implements the functionality for performing a health background check.
 */
@Service
public class BackgroundCheckService {
  @Autowired
  private DiagnosisRepository diagnosisRepository;
  @Autowired
  private InsurancePolicyRepository insurancePolicyRepository;
  @Autowired
  private PatientRepository patientRepository;

  /**
   * Run a complete BG check on the requested id.
   *
   * @param id ID string for the patient whose background is being checked
   * @param requestedVaccination name of the vaccination being checked for
   * @param requestedAllergy name of the allergy being checked for
   * @param requestedDiagnosis name of a diagnosis being checked for
   */
  public Map<String, Boolean> getBackgroundCheck(UUID id, String requestedVaccination,
                                                 String requestedAllergy,
                                                 String requestedDiagnosis) {
    // This implementation ignores irrelevant entries in `requirements`

    // Ensure a valid patient ID
    Optional<Patient> patientOption = patientRepository.findById(id);
    Patient patient = null;
    if (patientOption.isPresent()) {
      patient = patientOption.get();
    } else {
      return null;
    }

    boolean validVaccine = false;
    boolean validAllergy = false;
    boolean validDiagnosis = false;

    // Get and check records
    // String requestedVaccination = requirements.get("vaccination");
    if (requestedVaccination != null) {
      String vaccinations = patient.getVaccinations();

      if (vaccinations != null) {
        // Split vaccination string along delimiter ","
        // Note: Delimiter is guaranteed to be ","
        String[] vaccinationStrings =  vaccinations.split(",");

        for (String vaccination : vaccinationStrings) {
          if (requestedVaccination.trim().equalsIgnoreCase(vaccination.trim())) {
            validVaccine = true;
            break;
          }
        }
      }
    }

    // String requestedAllergy = requirements.get("allergy");
    if (requestedAllergy != null) {
      String allergies = patient.getAllergies();

      if (allergies != null) {
        // Split allergy string along delimiter ","
        // Note: Delimiter is guaranteed to be ","
        String[] allergyStrings =  allergies.split(",");

        for (String allergy: allergyStrings) {
          if (requestedAllergy.trim().equalsIgnoreCase(allergy.trim())) {
            validAllergy = true;
            break;
          }
        }
      }
    }

    // String requestedDiagnosis = requirements.get("diagnosis");
    if (requestedDiagnosis != null) {
      List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByPatientId(id);

      for (Diagnosis diagnosis : diagnoses) {
        if (diagnosis.getCondition().equalsIgnoreCase(requestedDiagnosis.trim())) {
          validDiagnosis = true;
          break;
        }
      }
    }

    Map<String, Boolean> result = new HashMap<>();

    // Return Map with all relevant data
    result.put("vaccination", validVaccine);
    result.put("allergy", validAllergy);
    result.put("diagnosis", validDiagnosis);

    return result;
  }
}
