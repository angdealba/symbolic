package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

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
        Patient subject = new Patient("flu", "peanut", null);

        // Mock input
        long request_id = subject.getId();
        String request_vaccinations = "measles";
        String request_allergies = "peanut";
        String request_


    }
}
