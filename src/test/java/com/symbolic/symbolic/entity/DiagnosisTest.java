package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
public class DiagnosisTest {
    private Diagnosis diagnosis;
    private Patient patient;
    private MedicalPractitioner practitioner;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @BeforeEach
    void setup() throws ParseException {
        // Create the diagnosis object and its associated patient and practitioner
        Date date = formatter.parse("2023-10-20");
        this.diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", date);

        this.patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        this.diagnosis.setPatient(this.patient);

        this.practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        this.diagnosis.setPractitioner(this.practitioner);
    }

    @Test
    void testConstructorAndGetters() throws ParseException {
        // Check equality of basic fields
        assertEquals(diagnosis.getCondition(), "COVID-19");
        assertEquals(diagnosis.getTreatmentInfo(), "Antiviral Medication");
        assertEquals(diagnosis.getDate(), formatter.parse("2023-10-20"));

        // Check equality of patient and practitioner objects after they are stored in the diagnosis
        assertEquals(diagnosis.getPatient(), patient);
        assertEquals(diagnosis.getPractitioner(), practitioner);
    }

    @Test
    void testSetters() throws ParseException {
        // Test each setter for the different fields
        UUID id = UUID.randomUUID();
        diagnosis.setId(id);
        assertEquals(diagnosis.getId(), id);

        diagnosis.setCondition("Influenza");
        assertEquals(diagnosis.getCondition(), "Influenza");

        diagnosis.setTreatmentInfo("Tamiflu");
        assertEquals(diagnosis.getTreatmentInfo(), "Tamiflu");

        diagnosis.setDate(formatter.parse("2023-10-21"));
        assertEquals(diagnosis.getDate(), formatter.parse("2023-10-21"));

        // Also check the setters for patient and practitioner
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        diagnosis.setPatient(newPatient);
        assertEquals(diagnosis.getPatient(), newPatient);

        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        diagnosis.setPractitioner(newPractitioner);
        assertEquals(diagnosis.getPractitioner(), newPractitioner);
    }

    @Test
    void testHashCode() {
        assertNotNull(diagnosis.hashCode());
    }

    @Test
    void testEquals() throws ParseException {
        // Base cases for testing simple equality
        assertEquals(diagnosis, diagnosis);
        assertNotEquals(diagnosis, "Test string");
        assertNotEquals(diagnosis, null);

        // Different objects with same values are equal
        Date date = formatter.parse("2023-10-20");
        Diagnosis diagnosis2 = new Diagnosis("COVID-19", "Antiviral Medication", date);
        diagnosis2.setPatient(patient);
        diagnosis2.setPractitioner(practitioner);
        assertEquals(diagnosis, diagnosis2);

        // Changes in different basic fields are not equal
        Diagnosis diagnosis3 = new Diagnosis("Influenza", "Antiviral Medication", date);
        diagnosis3.setPatient(patient);
        diagnosis3.setPractitioner(practitioner);
        assertNotEquals(diagnosis, diagnosis3);

        Diagnosis diagnosis4 = new Diagnosis("COVID-19", "In-Patient Treatment", date);
        diagnosis4.setPatient(patient);
        diagnosis4.setPractitioner(practitioner);
        assertNotEquals(diagnosis, diagnosis4);

        Date newDate = formatter.parse("2023-10-21");
        Diagnosis diagnosis5 = new Diagnosis("COVID-19", "Antiviral Medication", newDate);
        diagnosis5.setPatient(patient);
        diagnosis5.setPractitioner(practitioner);
        assertNotEquals(diagnosis, diagnosis5);

        // Different patients are not equal
        Diagnosis diagnosis6 = new Diagnosis("COVID-19", "Antiviral Medication", date);
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        diagnosis6.setPatient(newPatient);
        diagnosis6.setPractitioner(practitioner);
        assertNotEquals(diagnosis, diagnosis6);

        // Different practitioners are not equal
        Diagnosis diagnosis7 = new Diagnosis("COVID-19", "Antiviral Medication", date);
        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        diagnosis7.setPatient(patient);
        diagnosis7.setPractitioner(newPractitioner);
        assertNotEquals(diagnosis, diagnosis7);
    }

    @Test
    void testIDEquality() throws ParseException {
      // Different IDs are not equal
      UUID id1 = UUID.randomUUID();
      diagnosis.setId(id1);

      Date date = formatter.parse("2023-10-20");
      Diagnosis diagnosis2 = new Diagnosis("COVID-19", "Antiviral Medication", date);
      diagnosis2.setPatient(patient);
      diagnosis2.setPractitioner(practitioner);
      UUID id2 = UUID.randomUUID();
      diagnosis2.setId(id2);
      assertNotEquals(diagnosis, diagnosis2);
    }
}
