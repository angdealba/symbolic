package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
public class PrescriptionTest {
    private Prescription prescription;
    private Patient patient;
    private MedicalPractitioner practitioner;

    @BeforeEach
    void setup() {
        // Create the prescription object and its associated patient and practitioner
        this.prescription = new Prescription(1, 2, 100, "Test instructions");

        this.patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        this.prescription.setPatient(this.patient);

        this.practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        this.prescription.setPractitioner(this.practitioner);
    }

    @Test
    void testConstructorAndGetters() {
        // Check equality of basic fields
        assertEquals(prescription.getDosage(), 1);
        assertEquals(prescription.getDailyUses(), 2);
        assertEquals(prescription.getCost(), 100);
        assertEquals(prescription.getInstructions(), "Test instructions");

        // Check equality of patient and practitioner objects after they are stored in the prescription
        assertEquals(prescription.getPatient(), patient);
        assertEquals(prescription.getPractitioner(), practitioner);
    }

    @Test
    void testSetters() {
        // Test each setter for the different fields
        UUID id = UUID.randomUUID();
        prescription.setId(id);
        assertEquals(prescription.getId(), id);

        prescription.setDosage(2);
        assertEquals(prescription.getDosage(), 2);

        prescription.setDailyUses(3);
        assertEquals(prescription.getDailyUses(), 3);

        prescription.setCost(75);
        assertEquals(prescription.getCost(), 75);

        prescription.setInstructions("New test");
        assertEquals(prescription.getInstructions(), "New test");

        // Also check the setters for patient and practitioner
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        prescription.setPatient(newPatient);
        assertEquals(prescription.getPatient(), newPatient);

        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        prescription.setPractitioner(newPractitioner);
        assertEquals(prescription.getPractitioner(), newPractitioner);
    }

    @Test
    void testHashCode() {
        assertNotNull(prescription.hashCode());
    }

    @Test
    void testEquals() {
        // Base cases for testing simple equality
        assertEquals(prescription, prescription);
        assertNotEquals(prescription, "Test string");
        assertNotEquals(prescription, null);

        // Different objects with same values are equal
        Prescription prescription2 = new Prescription(1, 2, 100, "Test instructions");
        prescription2.setPatient(patient);
        prescription2.setPractitioner(practitioner);

        assertEquals(prescription, prescription2);

        // Changes in different basic fields are not equal
        Prescription prescription3 = new Prescription(2, 2, 100, "Test instructions");
        prescription3.setPatient(patient);
        prescription3.setPractitioner(practitioner);
        assertNotEquals(prescription, prescription3);

        Prescription prescription4 = new Prescription(1, 3, 100, "Test instructions");
        prescription4.setPatient(patient);
        prescription4.setPractitioner(practitioner);
        assertNotEquals(prescription, prescription4);

        Prescription prescription5 = new Prescription(1, 2, 200, "Test instructions");
        prescription5.setPatient(patient);
        prescription5.setPractitioner(practitioner);
        assertNotEquals(prescription, prescription5);

        Prescription prescription6 = new Prescription(1, 2, 100, "Different instructions");
        prescription6.setPatient(patient);
        prescription6.setPractitioner(practitioner);
        assertNotEquals(prescription, prescription6);

        // Different patients are not equal
        Prescription prescription7 = new Prescription(1, 2, 100, "Test instructions");
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        prescription7.setPatient(newPatient);
        prescription7.setPractitioner(practitioner);
        assertNotEquals(prescription, prescription7);

        // Different practitioners are not equal
        Prescription prescription8 = new Prescription(1, 2, 100, "Test instructions");
        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        prescription8.setPatient(patient);
        prescription8.setPractitioner(newPractitioner);
        assertNotEquals(prescription, prescription8);
    }

    @Test
    void testIDEquality() {
      // Different IDs are not equal
      UUID id1 = UUID.randomUUID();
      prescription.setId(id1);

      Prescription prescription2 = new Prescription(1, 2, 100, "Test instructions");
      prescription2.setPatient(patient);
      prescription2.setPractitioner(practitioner);
      UUID id2 = UUID.randomUUID();
      prescription2.setId(id2);
      assertNotEquals(prescription, prescription2);
    }
}
