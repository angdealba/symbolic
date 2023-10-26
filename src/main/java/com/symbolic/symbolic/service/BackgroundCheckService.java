package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.InsurancePolicyRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BackgroundCheckService {
    @Autowired
    private DiagnosisRepository diagnosisRepository;
    @Autowired
    private InsurancePolicyRepository insurancePolicyRepository;
    @Autowired
    private PatientRepository patientRepository;

    // Run a complete BG check on the requested id
    public Map<String, Boolean> getBGCheck(Long id, Map<String, String> requirements) {
        // This implementation ignores irrelevant entries in `requirements`

        Map<String, Boolean> result = new HashMap<>();

        // Ensure a valid patient ID
        Optional<Patient> patientOption = patientRepository.findById(id);
        Patient patient = null;
        if (patientOption.isPresent())
            patient = patientOption.get();
        else
            return null;

        boolean validVaccine = false;
        boolean validAllergy = false;
        boolean validDiagnosis = false;

        // Get and check records
        String requestedVaccination = requirements.get("vaccination");
        if (!requestedVaccination.isEmpty()) {
            String vaccinations = patient.getVaccinations();

            if (vaccinations.contains(requestedVaccination))
                validVaccine = true;
        }

        String requestedAllergy = requirements.get("allergy");
        if (!requestedAllergy.isEmpty()) {
            String allergies = patient.getAllergies();

            if (allergies.contains(requestedAllergy))
                validAllergy = true;
        }

        String requestedDiagnosis = requirements.get("diagnosis");
        if (!requestedDiagnosis.isEmpty()) {
            List<Diagnosis> diagnoses = diagnosisRepository.findDiagnosesByPatientId(id);

            for (Diagnosis diagnosis : diagnoses) {
                if (diagnosis.getCondition().equalsIgnoreCase(requestedDiagnosis)) {
                    validDiagnosis = true;
                    break;
                }
            }
        }

        // Return List with all relevant data
        // The controller will handle parsing relevant sections
        result.put("vaccination", validVaccine);
        result.put("allergy", validAllergy);
        result.put("diagnosis", validDiagnosis);

        return result;
    }
}
