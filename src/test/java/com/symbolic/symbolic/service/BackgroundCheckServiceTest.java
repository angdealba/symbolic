package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

public class BackgroundCheckServiceTest {

    @InjectMocks
    private BackgroundCheckService backgroundCheckService;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private PatientRepository patientRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Test for a request on three different requirements + id
    @Test
    public void testBackgroundCheck() {
        // Mock patient
        Patient patient = new Patient("flu", "peanut", null);

        // Mock diagnosis
        Diagnosis diagnosis = new Diagnosis("salmonella", "antibiotics", new Date());
        List<Diagnosis> diag_list = new ArrayList<>();
        diag_list.add(diagnosis);

        // Mock input
        UUID request_id = UUID.randomUUID();
        String request_vaccinations = "measles";
        String request_allergies = "peanut";
        String request_diagnosis = "covid";

        // Mock operations
        when(patientRepository.findById(request_id)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.findDiagnosesByPatientId(request_id)).thenReturn(diag_list);

        // Test service
        Map<String, Boolean> result = backgroundCheckService.getBackgroundCheck(request_id, request_vaccinations,
                request_allergies, request_diagnosis);

        // Assert correctness
        assertNotEquals(null, result);
        assertEquals(false, result.get("vaccination"));
        assertEquals(true, result.get("allergy"));
        assertEquals(false, result.get("diagnosis"));
    }
}
