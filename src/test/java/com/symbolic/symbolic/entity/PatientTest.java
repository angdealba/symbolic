package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {
    private Patient patient;
    private InsurancePolicy policy;
    private Set<MedicalPractitioner> practitioners = new HashSet<>();
    private Set<Facility> facilities = new HashSet<>();
    private Set<Appointment> appointments = new HashSet<>();
    private Set<Prescription> prescriptions = new HashSet<>();
    private Set<Diagnosis> diagnoses = new HashSet<>();

    private SimpleDateFormat apt_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat dia_formatter = new SimpleDateFormat("yyyy-MM-dd");

    // Helper method for generating copies of the Patient data to a new object
    void copyPatientData(Patient newPatient) {
        newPatient.setInsurancePolicy(policy);

        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            newPatient.addPractitioner(tempPractitioner);
        }
        for (Iterator<Facility> it = facilities.iterator(); it.hasNext(); ) {
            Facility tempFacility = it.next();
            newPatient.addFacility(tempFacility);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            newPatient.addAppointment(tempAppointment);
        }
        for (Iterator<Prescription> it = prescriptions.iterator(); it.hasNext(); ) {
            Prescription tempPrescription = it.next();
            newPatient.addPrescription(tempPrescription);
        }
        for (Iterator<Diagnosis> it = diagnoses.iterator(); it.hasNext(); ) {
            Diagnosis tempDiagnosis = it.next();
            newPatient.addDiagnosis(tempDiagnosis);
        }
    }

    @BeforeEach
    void setup() throws ParseException {
        // Build the first Patient object including linked sets for practitioner, facility, appointment, prescription, and diagnosis objects  and a linked policy
        this.patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");

        this.policy = new InsurancePolicy(100);
        this.patient.setInsurancePolicy(this.policy);

        MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        this.practitioners.add(practitioner1);
        this.practitioners.add(practitioner2);
        this.patient.addPractitioner(practitioner1);
        this.patient.addPractitioner(practitioner2);

        Facility facility1 = new Facility(40.7, 74.0, "Surgery");
        Facility facility2 = new Facility(40.71, 74.01, "Optometry");
        this.facilities.add(facility1);
        this.facilities.add(facility2);
        this.patient.addFacility(facility1);
        this.patient.addFacility(facility2);

        Date apt_date1 = apt_formatter.parse("2023-10-20 12:00");
        Appointment appointment1 = new Appointment(apt_date1, 100);
        Date apt_date2 = apt_formatter.parse("2023-10-21 2:30");
        Appointment appointment2 = new Appointment(apt_date2, 200);
        this.appointments.add(appointment1);
        this.appointments.add(appointment2);
        this.patient.addAppointment(appointment1);
        this.patient.addAppointment(appointment2);

        Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
        Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
        this.prescriptions.add(prescription1);
        this.prescriptions.add(prescription2);
        this.patient.addPrescription(prescription1);
        this.patient.addPrescription(prescription2);

        Date dia_date1 = dia_formatter.parse("2023-10-20");
        Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", dia_date1);
        Date dia_date2 = dia_formatter.parse("2023-10-21");
        Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", dia_date2);
        this.diagnoses.add(diagnosis1);
        this.diagnoses.add(diagnosis2);
        this.patient.addDiagnosis(diagnosis1);
        this.patient.addDiagnosis(diagnosis2);
    }

    @Test
    void testConstructorAndGetters() {
        // Check equality of basic fields
        assertEquals(patient.getVaccinations(), "COVID-19");
        assertEquals(patient.getAllergies(), "Dairy");
        assertEquals(patient.getAccommodations(), "Wheelchair Access");

        // Check equality of policy, practitioner, facility, appointment, prescription, and diagnosis objects after they are stored in the patient
        assertEquals(patient.getInsurancePolicy(), policy);

        assertEquals(patient.getPractitioners().size(), 2);
        assertEquals(patient.getPractitioners(), practitioners);

        assertEquals(patient.getFacilities().size(), 2);
        assertEquals(patient.getFacilities(), facilities);

        assertEquals(patient.getAppointments().size(), 2);
        assertEquals(patient.getAppointments(), appointments);

        assertEquals(patient.getPrescriptions().size(), 2);
        assertEquals(patient.getPrescriptions(), prescriptions);

        assertEquals(patient.getDiagnoses().size(), 2);
        assertEquals(patient.getDiagnoses(), diagnoses);
    }

    @Test
    void testSetters() {
        // Test each setter for the different fields
        patient.setVaccinations("Flu");
        assertEquals(patient.getVaccinations(), "Flu");

        patient.setAllergies("Tree Nuts");
        assertEquals(patient.getAllergies(), "Tree Nuts");

        patient.setAccommodations("None");
        assertEquals(patient.getAccommodations(), "None");
    }

    @Test
    void testPolicyModification() {
        // Test that modifying the policy object correctly changes the practitioner's policy
        InsurancePolicy newPolicy = new InsurancePolicy(200);
        patient.setInsurancePolicy(newPolicy);
        assertEquals(patient.getInsurancePolicy(), newPolicy);
    }

    @Test
    void testPractitionerModification() {
        MedicalPractitioner practitioner3 = new MedicalPractitioner(40.72, 74.02, "Dentistry", 75, 15);
        patient.addPractitioner(practitioner3);

        // Test that the patient's practitioners set contains the new addition
        Set<MedicalPractitioner> newPractitioners = patient.getPractitioners();
        assertTrue(newPractitioners.contains(practitioner3));

        // Test that the practitioners set no longer contains a practitioner after running the removal
        MedicalPractitioner toRemove = (MedicalPractitioner) newPractitioners.toArray()[0];

        patient.removePractitionerById(toRemove.getId());
        assertFalse(patient.getPractitioners().contains(toRemove));
    }

    @Test
    void testFacilityModification() {
        Facility facility3 = new Facility(40.72, 74.02, "Dentistry");
        patient.addFacility(facility3);

        // Test that the patient's facilities set contains the new addition
        Set<Facility> newFacilities = patient.getFacilities();
        assertTrue(newFacilities.contains(facility3));

        // Test that the facilities set no longer contains a facility after running the removal command
        Facility toRemove = (Facility) newFacilities.toArray()[0];

        patient.removeFacilityById(toRemove.getId());
        assertFalse(patient.getFacilities().contains(toRemove));
    }

    @Test
    void testAppointmentModification() throws ParseException {
        Date date3 = apt_formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        patient.addAppointment(appointment3);

        // Test that the patient's appointments set contains the new addition
        Set<Appointment> newAppointments = patient.getAppointments();
        assertTrue(newAppointments.contains(appointment3));

        // Test that the appointments set no longer contains an appointment after running the removal command
        Appointment toRemove = (Appointment) newAppointments.toArray()[0];

        patient.removeAppointmentById(toRemove.getId());
        assertFalse(patient.getAppointments().contains(toRemove));
    }

    @Test
    void testPrescriptionModification() {
        Prescription prescription3 = new Prescription(3, 1, 200, "New test");
        patient.addPrescription(prescription3);

        // Test that the patient's prescriptions set contains the new addition
        Set<Prescription> newPrescriptions = patient.getPrescriptions();
        assertTrue(newPrescriptions.contains(prescription3));

        // Test that the prescriptions set no longer contains a prescription after running the removal command
        Prescription toRemove = (Prescription) newPrescriptions.toArray()[0];

        patient.removePrescriptionById(toRemove.getId());
        assertFalse(patient.getPrescriptions().contains(toRemove));
    }

    @Test
    void testDiagnosisModification() throws ParseException {
        Date date3 = dia_formatter.parse("2023-10-23");
        Diagnosis diagnosis3 = new Diagnosis("Common Cold", "None", date3);
        patient.addDiagnosis(diagnosis3);

        // Test that the patient's diagnosis set contains the new addition
        Set<Diagnosis> newDiagnoses = patient.getDiagnoses();
        assertTrue(newDiagnoses.contains(diagnosis3));

        // Test that the diagnoses set no longer contains a diagnosis after running the removal command
        Diagnosis toRemove = (Diagnosis) newDiagnoses.toArray()[0];

        patient.removeDiagnosisById(toRemove.getId());
        assertFalse(patient.getDiagnoses().contains(toRemove));
    }

    @Test
    void testHashCode() {
        assertNotNull(patient.hashCode());
    }

    @Test
    void testBasicEquality() {
        // Base cases for testing simple equality
        assertEquals(patient, patient);
        assertNotEquals(patient, "Test string");

        // Different objects with same values are equal
        Patient patient2 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        copyPatientData(patient2);
        assertEquals(patient, patient2);
    }

    @Test
    void testFieldEquality() {
        // Different vaccination values are not equal
        Patient patient3 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient3);
        assertNotEquals(patient, patient3);

        // Different allergy values are not equal
        Patient patient4 = new Patient("COVID-19", "Tree Nuts", "Wheelchair Access");
        copyPatientData(patient4);
        assertNotEquals(patient, patient4);

        // Different accommodation values are not equal
        Patient patient5 = new Patient("COVID-19", "Dairy", "None");
        copyPatientData(patient5);
        assertNotEquals(patient, patient5);
    }

    @Test
    void testPractitionerEquality() {
        // Different practitioners are not equal
        Patient patient6 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient6);

        MedicalPractitioner practitioner3 = new MedicalPractitioner(40.72, 74.02, "Dentistry", 75, 15);
        patient6.addPractitioner(practitioner3);
        assertNotEquals(patient, patient6);
    }

    @Test
    void testFacilityEquality() {
        // Different facilities are not equal
        Patient patient7 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient7);

        Facility facility3 = new Facility(40.72, 74.02, "Dentistry");
        patient7.addFacility(facility3);
        assertNotEquals(patient, patient7);
    }

    @Test
    void testAppointmentEquality() throws ParseException {
        // Different appointments are not equal
        Patient patient8 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient8);

        Date date3 = apt_formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        patient8.addAppointment(appointment3);
        assertNotEquals(patient, patient8);
    }

    @Test
    void testPrescriptionEquality() {
        // Different patients are not equal
        Patient patient9 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient9);

        Prescription prescription3 = new Prescription(3, 1, 200, "New test");
        patient9.addPrescription(prescription3);
        assertNotEquals(patient, patient9);
    }

    @Test
    void testDiagnosisEquality() throws ParseException {
        // Different appointments are not equal
        Patient patient10 = new Patient("Flu", "Dairy", "Wheelchair Access");
        copyPatientData(patient10);

        Date date3 = dia_formatter.parse("2023-10-23");
        Diagnosis diagnosis3 = new Diagnosis("Common Cold", "None", date3);
        patient10.addDiagnosis(diagnosis3);
        assertNotEquals(patient, patient10);
    }
}
