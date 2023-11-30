package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
public class MedicalPractitionerServiceTest {

    @InjectMocks
    private MedicalPractitionerService medicalPractitionerService;

    @Mock
    private MedicalPractitionerRepository medicalPractitionerRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSearchMedicalPractitioner() {
        // Set up mock data
        Double latitude = 40.0;
        Double longitude = 74.0;
        String specialization = "Surgery";
        Integer cost = 50;
        Integer years = 10;
        MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        // Mock search on facilityRepository
        Double minLatitude = latitude - 0.1;
        Double maxLatitude = latitude + 0.1;
        Double minLongitude = longitude - 0.1;
        Double maxLongitude = longitude + 0.1;

        when(medicalPractitionerRepository.findByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude))
                .thenReturn((List<MedicalPractitioner>) Arrays.asList(practitioner1, practitioner2));


        // Mock Test
        List<MedicalPractitioner> result = medicalPractitionerService.search(latitude, longitude, specialization, cost, years);

        assertEquals(1, result.size());
        assertEquals("Surgery", result.get(0).getSpecialization());
        assertEquals(50, result.get(0).getConsultationCost());
        assertEquals(10, result.get(0).getYearsExperience());
    }
}
