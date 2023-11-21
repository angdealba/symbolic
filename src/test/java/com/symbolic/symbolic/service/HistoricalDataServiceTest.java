package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class HistoricalDataServiceTest {

    @InjectMocks
    private HistoricalDataService historicalDataService;

    @Mock
    private DiagnosisRepository diagnosisRepository;
    @Mock
    private MedicalPractitionerRepository medicalPractitionerRepository;
    private Diagnosis diagnosis, diagnosis2, diagnosis3, diagnosis4, diagnosis5;
    private Patient patient;
    private MedicalPractitioner practitioner;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private List<Double> location = new ArrayList<>();

    Date begin;
    Date end;

    Date date;

   /* @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }*/

    @BeforeEach
    void setup() throws ParseException {
        MockitoAnnotations.openMocks(this);
        // Create the diagnosis object and its associated patient and practitioner
        date = formatter.parse("2023-10-20");
        diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", date);

        patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        diagnosis.setPatient(patient);

        practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        diagnosis.setPractitioner(practitioner);

        location.add(40.7);
        location.add(74.0);
        begin = formatter.parse("2023-10-10");
        end = formatter.parse("2023-10-30");

        diagnosis2 = new Diagnosis("COVID-19", "Antiviral Medication", date);
        diagnosis2.setPractitioner(practitioner);
        diagnosis3 = new Diagnosis("COVID-19", "Antiviral Medication", date);
        diagnosis3.setPractitioner(practitioner);
        diagnosis4= new Diagnosis("Influenza", "Tamiflu", date);
        diagnosis4.setPractitioner(practitioner);
        diagnosis5 = new Diagnosis("Flu", "Antiviral Medication", date);
        diagnosis5.setPractitioner(practitioner);


    }

    @Test
    public void testHistoricalDataByCondition() {

        when(diagnosisRepository.findDiagnosesByConditionIgnoreCase("COVID 19"))
                .thenReturn((List<Diagnosis>) Arrays.asList(diagnosis));

        Double latitude = 40.7;
        Double longitude = 74.0;
        Double minLatitude = latitude - 0.1;
        Double maxLatitude = latitude + 0.1;
        Double minLongitude = longitude - 0.1;
        Double maxLongitude = longitude + 0.1;


        when(medicalPractitionerRepository.findByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude))
                .thenReturn((List<MedicalPractitioner>) Arrays.asList(practitioner));

        // Mock Test
        List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition("COVID 19", begin, end, location);

        assertEquals(1, result.size());
        assertEquals("COVID-19", result.get(0).getCondition());
        assertEquals("Antiviral Medication", result.get(0).getTreatmentInfo());
        assertEquals(date, result.get(0).getDate());
    }

    @Test
    public void testHistoricalDataTopConditions() {

        when(diagnosisRepository.findAll())
                .thenReturn((List<Diagnosis>) Arrays.asList(diagnosis, diagnosis2, diagnosis3, diagnosis4, diagnosis5));

        Double latitude = 40.7;
        Double longitude = 74.0;
        Double minLatitude = latitude - 0.1;
        Double maxLatitude = latitude + 0.1;
        Double minLongitude = longitude - 0.1;
        Double maxLongitude = longitude + 0.1;


        when(medicalPractitionerRepository.findByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude))
                .thenReturn((List<MedicalPractitioner>) Arrays.asList(practitioner));

        // Mock Test
        Map<String, Integer> result = historicalDataService.getTopConditions(location, begin, end, 3);

        assertEquals(3, result.size());
        assertTrue(result.containsKey("COVID-19"));
        assertEquals(3, result.get("COVID-19"));
        assertTrue(result.containsKey("Influenza"));
        assertEquals(1, result.get("Influenza"));
        assertTrue(result.containsKey("Flu"));
        assertEquals(1, result.get("Flu"));

    }
}