package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
public class MedicalPractitionerTest {
    private MedicalPractitioner practitioner;
    private Facility facility;
    private Set<Patient> patients = new HashSet<>();
    private Set<Appointment> appointments = new HashSet<>();
    private Set<Prescription> prescriptions = new HashSet<>();
    private Set<Diagnosis> diagnoses = new HashSet<>();

    private SimpleDateFormat apt_formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat dia_formatter = new SimpleDateFormat("yyyy-MM-dd");

    // Helper method for generating copies of the MedicalPractitioner data to a new object
    void copyPractitionerData(MedicalPractitioner newPractitioner) {
        newPractitioner.setFacility(facility);
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            newPractitioner.addPatient(tempPatient);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            newPractitioner.addAppointment(tempAppointment);
        }
        for (Iterator<Prescription> it = prescriptions.iterator(); it.hasNext(); ) {
            Prescription tempPrescription = it.next();
            newPractitioner.addPrescription(tempPrescription);
        }
        for (Iterator<Diagnosis> it = diagnoses.iterator(); it.hasNext(); ) {
            Diagnosis tempDiagnosis = it.next();
            newPractitioner.addDiagnosis(tempDiagnosis);
        }
    }

    @BeforeEach
    void setup() throws ParseException {
        // Build the first MedicalPractitioner object including linked sets for patient, appointment, prescription, and diagnoses and a linked facility
        this.practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);

        this.facility = new Facility(40.7, 74.0, "Surgery");
        this.practitioner.setFacility(this.facility);

        Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        Patient patient2 = new Patient("Flu", "Tree Nut", "None");
        this.patients.add(patient1);
        this.patients.add(patient2);
        this.practitioner.addPatient(patient1);
        this.practitioner.addPatient(patient2);

        Date apt_date1 = apt_formatter.parse("2023-10-20 12:00");
        Appointment appointment1 = new Appointment(apt_date1, 100);
        Date apt_date2 = apt_formatter.parse("2023-10-21 2:30");
        Appointment appointment2 = new Appointment(apt_date2, 200);
        this.appointments.add(appointment1);
        this.appointments.add(appointment2);
        this.practitioner.addAppointment(appointment1);
        this.practitioner.addAppointment(appointment2);

        Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
        Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
        this.prescriptions.add(prescription1);
        this.prescriptions.add(prescription2);
        this.practitioner.addPrescription(prescription1);
        this.practitioner.addPrescription(prescription2);

        Date dia_date1 = dia_formatter.parse("2023-10-20");
        Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", dia_date1);
        Date dia_date2 = dia_formatter.parse("2023-10-21");
        Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", dia_date2);
        this.diagnoses.add(diagnosis1);
        this.diagnoses.add(diagnosis2);
        this.practitioner.addDiagnosis(diagnosis1);
        this.practitioner.addDiagnosis(diagnosis2);
    }

    @Test
    void testConstructorAndGetters() {
        // Check equality of basic fields
        assertEquals(practitioner.getLatitude(), 40.7);
        assertEquals(practitioner.getLongitude(), 74.0);
        assertEquals(practitioner.getSpecialization(), "Surgery");
        assertEquals(practitioner.getConsultationCost(), 50);
        assertEquals(practitioner.getYearsExperience(), 10);

        // Check equality of getLocation helper return
        Pair<Double, Double> coords = Pair.of(40.7, 74.0);
        assertEquals(practitioner.getLocation(), coords);

        // Check equality of facility, patient, appointment, prescription, and diagnosis objects after they are stored in the practitioner
        assertEquals(practitioner.getFacility(), facility);

        assertEquals(practitioner.getPatients().size(), 2);
        assertEquals(practitioner.getPatients(), patients);

        assertEquals(practitioner.getAppointments().size(), 2);
        assertEquals(practitioner.getAppointments(), appointments);

        assertEquals(practitioner.getPrescriptions().size(), 2);
        assertEquals(practitioner.getPrescriptions(), prescriptions);

        assertEquals(practitioner.getDiagnoses().size(), 2);
        assertEquals(practitioner.getDiagnoses(), diagnoses);
    }

    @Test
    void testSetters() {
        // Test each setter for the different fields
        UUID id = UUID.randomUUID();
        practitioner.setId(id);
        assertEquals(practitioner.getId(), id);

        practitioner.setLatitude(40.71);
        assertEquals(practitioner.getLatitude(), 40.71);

        practitioner.setLongitude(74.01);
        assertEquals(practitioner.getLongitude(), 74.01);

        practitioner.setSpecialization("Optometry");
        assertEquals(practitioner.getSpecialization(), "Optometry");

        practitioner.setConsultationCost(100);
        assertEquals(practitioner.getConsultationCost(), 100);

        practitioner.setYearsExperience(20);
        assertEquals(practitioner.getYearsExperience(), 20);

        // Check equality is preserved by setLocation() helper
        Pair<Double, Double> coords = Pair.of(40.72, 74.02);
        practitioner.setLocation(coords);
        assertEquals(practitioner.getLatitude(), 40.72);
        assertEquals(practitioner.getLongitude(), 74.02);
    }

    @Test
    void testFacilityModification() {
        // Test that modifying the facility object correctly changes the practitioner's facility
        Facility newFacility = new Facility(40.71, 74.01, "Optometry");
        practitioner.setFacility(newFacility);
        assertEquals(practitioner.getFacility(), newFacility);
    }

    @Test
    void testPatientModification() {
        Patient patient3 = new Patient("Measles", "None", "Deaf");
        practitioner.addPatient(patient3);

        // Test that the practitioner's patients set contains the new addition
        Set<Patient> newPatients = practitioner.getPatients();
        assertTrue(newPatients.contains(patient3));

        // Test that the patients set no longer contains a patient after running the removal command
        Patient toRemove = (Patient) newPatients.toArray()[0];

        practitioner.removePatientById(toRemove.getId());
        assertFalse(practitioner.getPatients().contains(toRemove));

        // Null removal does not crash
        UUID id = UUID.randomUUID();
        practitioner.removePatientById(id);
    }

    @Test
    void testAppointmentModification() throws ParseException {
        Date date3 = apt_formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        practitioner.addAppointment(appointment3);

        // Test that the practitioner's appointments set contains the new addition
        Set<Appointment> newAppointments = practitioner.getAppointments();
        assertTrue(newAppointments.contains(appointment3));

        // Test that the appointments set no longer contains an appointment after running the removal command
        Appointment toRemove = (Appointment) newAppointments.toArray()[0];

        practitioner.removeAppointmentById(toRemove.getId());
        assertFalse(practitioner.getAppointments().contains(toRemove));

        // Null removal does not crash
        UUID id = UUID.randomUUID();
        practitioner.removeAppointmentById(id);
    }

    @Test
    void testPrescriptionModification() {
        Prescription prescription3 = new Prescription(3, 1, 200, "New test");
        practitioner.addPrescription(prescription3);

        // Test that the practitioner's prescriptions set contains the new addition
        Set<Prescription> newPrescriptions = practitioner.getPrescriptions();
        assertTrue(newPrescriptions.contains(prescription3));

        // Test that the prescriptions set no longer contains a prescription after running the removal command
        Prescription toRemove = (Prescription) newPrescriptions.toArray()[0];

        practitioner.removePrescriptionById(toRemove.getId());
        assertFalse(practitioner.getPrescriptions().contains(toRemove));

        // Null removal does not crash
        UUID id = UUID.randomUUID();
        practitioner.removePrescriptionById(id);
    }

    @Test
    void testDiagnosisModification() throws ParseException {
        Date date3 = dia_formatter.parse("2023-10-23");
        Diagnosis diagnosis3 = new Diagnosis("Common Cold", "None", date3);
        practitioner.addDiagnosis(diagnosis3);

        // Test that the practitioner's diagnosis set contains the new addition
        Set<Diagnosis> newDiagnoses = practitioner.getDiagnoses();
        assertTrue(newDiagnoses.contains(diagnosis3));

        // Test that the diagnoses set no longer contains a diagnosis after running the removal command
        Diagnosis toRemove = (Diagnosis) newDiagnoses.toArray()[0];

        practitioner.removeDiagnosisById(toRemove.getId());
        assertFalse(practitioner.getDiagnoses().contains(toRemove));

        // Null removal does not crash
        UUID id = UUID.randomUUID();
        practitioner.removeDiagnosisById(id);
    }

    @Test
    void testHashCode() {
        assertNotNull(practitioner.hashCode());
    }

    @Test
    void testBasicEquality() {
        // Base cases for testing simple equality
        assertEquals(practitioner, practitioner);
        assertNotEquals(practitioner, "Test string");
        assertNotEquals(practitioner, null);

        // Different objects with same values are equal
        MedicalPractitioner practitioner2 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        copyPractitionerData(practitioner2);
        assertEquals(practitioner, practitioner2);
    }

    @Test
    void testFieldEquality() {
        // Different latitude values are not equal
        MedicalPractitioner practitioner3 = new MedicalPractitioner(40.71, 74.0, "Surgery", 50, 10);
        copyPractitionerData(practitioner3);
        assertNotEquals(practitioner, practitioner3);

        // Different longitude values are not equal
        MedicalPractitioner practitioner4 = new MedicalPractitioner(40.7, 74.01, "Surgery", 50, 10);
        copyPractitionerData(practitioner4);
        assertNotEquals(practitioner, practitioner4);

        // Different specialization values are not equal
        MedicalPractitioner practitioner5 = new MedicalPractitioner(40.7, 74.0, "Optometry", 50, 10);
        copyPractitionerData(practitioner5);
        assertNotEquals(practitioner, practitioner5);

        // Different consultation cost values are not equal
        MedicalPractitioner practitioner6 = new MedicalPractitioner(40.7, 74.0, "Surgery", 100, 10);
        copyPractitionerData(practitioner6);
        assertNotEquals(practitioner, practitioner6);

        // Different years experience values are not equal
        MedicalPractitioner practitioner7 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 20);
        copyPractitionerData(practitioner7);
        assertNotEquals(practitioner, practitioner7);
    }

    @Test
    void testPatientEquality() {
        // Different patients are not equal
        MedicalPractitioner practitioner8 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 20);
        copyPractitionerData(practitioner8);

        Patient patient3 = new Patient("Measles", "None", "Deaf");
        practitioner8.addPatient(patient3);
        assertNotEquals(practitioner, practitioner8);
    }

    @Test
    void testAppointmentEquality() throws ParseException {
        // Different appointments are not equal
        MedicalPractitioner practitioner9 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 20);
        copyPractitionerData(practitioner9);

        Date date3 = apt_formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        practitioner9.addAppointment(appointment3);
        assertNotEquals(practitioner, practitioner9);
    }

    @Test
    void testPrescriptionEquality() {
        // Different patients are not equal
        MedicalPractitioner practitioner10 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 20);
        copyPractitionerData(practitioner10);

        Prescription prescription3 = new Prescription(3, 1, 200, "New test");
        practitioner10.addPrescription(prescription3);
        assertNotEquals(practitioner, practitioner10);
    }

    @Test
    void testDiagnosisEquality() throws ParseException {
        // Different appointments are not equal
        MedicalPractitioner practitioner11 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 20);
        copyPractitionerData(practitioner11);

        Date date3 = dia_formatter.parse("2023-10-23");
        Diagnosis diagnosis3 = new Diagnosis("Common Cold", "None", date3);
        practitioner11.addDiagnosis(diagnosis3);
        assertNotEquals(practitioner, practitioner11);
    }

    @Test
    void testIDEquality() {
      // Different IDs are not equal
      UUID id1 = UUID.randomUUID();
      practitioner.setId(id1);

      MedicalPractitioner practitioner2 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
      copyPractitionerData(practitioner2);
      UUID id2 = UUID.randomUUID();
      practitioner2.setId(id2);
      assertNotEquals(practitioner, practitioner2);
    }
}
