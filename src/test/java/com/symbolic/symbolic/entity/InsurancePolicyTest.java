package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.cert.PolicyNode;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class InsurancePolicyTest {
    private InsurancePolicy policy;

    @BeforeEach
    void setup() {
        // Create the policy object and its associated patients
        this.policy = new InsurancePolicy(100);
        Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        Patient patient2 = new Patient("Flu", "Tree Nut", "None");

        this.policy.addPatient(patient1);
        this.policy.addPatient(patient2);
    }

    @Test
    void testConstructorAndGetters() {
        // Check equality of basic fields
        assertEquals(policy.getPremiumCost(), 100);

        // Check equality of patient data after it is stored in the Policy object
        Set<Patient> patients = new HashSet<>();
        Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        Patient patient2 = new Patient("Flu", "Tree Nut", "None");
        patients.add(patient1);
        patients.add(patient2);

        assertEquals(policy.getPatients().size(), 2);
        assertEquals(policy.getPatients(), patients);
    }

    @Test
    void testSetters() {
        // Test each setter for the different fields
        policy.setPremiumCost(200);
        assertEquals(policy.getPremiumCost(), 200);
    }

    @Test
    void testPatientModification() {
        Patient patient3 = new Patient("Measles", "None", "Deaf");
        policy.addPatient(patient3);

        // Test that the policy's patients set contains the new addition
        Set<Patient> patients = policy.getPatients();
        assertTrue(patients.contains(patient3));

        Set<Patient> newPatients = policy.getPatients();
        Patient toRemove = (Patient) newPatients.toArray()[0];

        // Test that the patients set no longer contains a patient after running the removal command
        policy.removePatientById(toRemove.getId());
        assertFalse(policy.getPatients().contains(toRemove));
    }

    @Test
    void testHashCode() {
        assertNotNull(policy.hashCode());
    }

    @Test
    void testEquals() {

        Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        Patient patient2 = new Patient("Flu", "Tree Nut", "None");

        // Base cases for testing simple equality
        InsurancePolicy policy1 = new InsurancePolicy(100);
        policy1.addPatient(patient1);
        policy1.addPatient(patient2);

        assertEquals(policy1, policy1);
        assertNotEquals(policy1, "Test string");

        // Different objects with same values are equal
        InsurancePolicy policy2 = new InsurancePolicy(100);
        policy2.addPatient(patient1);
        policy2.addPatient(patient2);

        assertEquals(policy1, policy2);

        // Different premium costs are not equal
        InsurancePolicy policy3 = new InsurancePolicy(200);
        policy1.addPatient(patient1);
        policy2.addPatient(patient2);

        assertNotEquals(policy1, policy3);

        // Different patient sets are not equal
        InsurancePolicy policy4 = new InsurancePolicy(100);
        policy4.addPatient(patient1);

        assertNotEquals(policy1, policy4);
    }
}
