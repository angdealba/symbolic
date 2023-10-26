package com.symbolic.symbolic.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FacilityTest {
    private Facility facility;
    private Set<Appointment> appointments = new HashSet<>();
    private Set<Patient> patients = new HashSet<>();
    private Set<MedicalPractitioner> practitioners = new HashSet<>();

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setup() throws ParseException {
        // Build the first Facility object including linked sets for patient, practitioner, and appointment
        this.facility = new Facility(40.7, 74.0, "Surgery");

        Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        Patient patient2 = new Patient("Flu", "Tree Nut", "None");
        this.patients.add(patient1);
        this.patients.add(patient2);
        this.facility.addPatient(patient1);
        this.facility.addPatient(patient2);

        MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        this.practitioners.add(practitioner1);
        this.practitioners.add(practitioner2);
        this.facility.addPractitioner(practitioner1);
        this.facility.addPractitioner(practitioner2);

        Date date1 = formatter.parse("2023-10-20 12:00");
        Appointment appointment1 = new Appointment(date1, 100);
        Date date2 = formatter.parse("2023-10-21 2:30");
        Appointment appointment2 = new Appointment(date2, 200);
        this.appointments.add(appointment1);
        this.appointments.add(appointment2);
        this.facility.addAppointment(appointment1);
        this.facility.addAppointment(appointment2);
    }

    @Test
    void testConstructorAndGetters() {
        // Check equality of basic fields
        assertEquals(facility.getLatitude(), 40.7);
        assertEquals(facility.getLongitude(), 74.0);
        assertEquals(facility.getSpecialization(), "Surgery");

        // Check equality of getLocation helper return
        Pair<Double, Double> coords = Pair.of(40.7, 74.0);
        assertEquals(facility.getLocation(), coords);

        // Check equality of patient, practitioner, and appointment objects after they are stored in the facility
        assertEquals(facility.getPatients().size(), 2);
        assertEquals(facility.getPatients(), patients);

        assertEquals(facility.getPractitioners().size(), 2);
        assertEquals(facility.getPractitioners(), practitioners);

        assertEquals(facility.getAppointments().size(), 2);
        assertEquals(facility.getAppointments(), appointments);
    }

    @Test
    void testSetters() {
        // Test each setter for the different fields
        facility.setLatitude(40.71);
        assertEquals(facility.getLatitude(), 40.71);

        facility.setLongitude(74.01);
        assertEquals(facility.getLongitude(), 74.01);

        facility.setSpecialization("Optometry");
        assertEquals(facility.getSpecialization(), "Optometry");

        // Check equality is preserved by setLocation() helper
        Pair<Double, Double> coords = Pair.of(40.72, 74.02);
        facility.setLocation(coords);
        assertEquals(facility.getLatitude(), 40.72);
        assertEquals(facility.getLongitude(), 74.02);
    }

    @Test
    void testPatientModification() {
        Patient patient3 = new Patient("Measles", "None", "Deaf");
        facility.addPatient(patient3);

        // Test that the facility's patients set contains the new addition
        Set<Patient> newPatients = facility.getPatients();
        assertTrue(newPatients.contains(patient3));

        // Test that the patients set no longer contains a patient after running the removal command
        Patient toRemove = (Patient) newPatients.toArray()[0];

        facility.removePatientById(toRemove.getId());
        assertFalse(facility.getPatients().contains(toRemove));
    }

    @Test
    void testPractitionerModification() {
        MedicalPractitioner practitioner3 = new MedicalPractitioner(40.72, 74.02, "Dentistry", 75, 15);
        facility.addPractitioner(practitioner3);

        // Test that the facility's practitioners set contains the new addition
        Set<MedicalPractitioner> newPractitioners = facility.getPractitioners();
        assertTrue(newPractitioners.contains(practitioner3));

        // Test that the practitioners set no longer contains a practitioner after running the removal
        MedicalPractitioner toRemove = (MedicalPractitioner) newPractitioners.toArray()[0];

        facility.removePractitionerById(toRemove.getId());
        assertFalse(facility.getPractitioners().contains(toRemove));
    }

    @Test
    void testAppointmentModification() throws ParseException {
        Date date3 = formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        facility.addAppointment(appointment3);

        // Test that the facility's appointments set contains the new addition
        Set<Appointment> newAppointments = facility.getAppointments();
        assertTrue(newAppointments.contains(appointment3));

        // Test that the appointments set no longer contains an appointment after running the removal command
        Appointment toRemove = (Appointment) newAppointments.toArray()[0];

        facility.removeAppointmentById(toRemove.getId());
        assertFalse(facility.getAppointments().contains(toRemove));
    }

    @Test
    void testHashCode() {
        assertNotNull(facility.hashCode());
    }

    @Test
    void testBasicEquality() {
        // Base cases for testing simple equality
        assertEquals(facility, facility);
        assertNotEquals(appointments, "Test string");

        // Different objects with same values are equal
        Facility facility2 = new Facility(40.7, 74.0, "Surgery");

        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility2.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility2.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility2.addAppointment(tempAppointment);
        }
        assertEquals(facility, facility2);
    }

    @Test
    void testFieldEquality() {
        // Different latitude values are not equal
        Facility facility3 = new Facility(40.71, 74.0, "Surgery");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility3.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility3.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility3.addAppointment(tempAppointment);
        }
        assertNotEquals(facility, facility3);

        // Different longitude values are not equal
        Facility facility4 = new Facility(40.70, 74.01, "Surgery");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility4.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility4.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility4.addAppointment(tempAppointment);
        }
        assertNotEquals(facility, facility4);

        // Different specialization values are not equal
        Facility facility5 = new Facility(40.70, 74.0, "Optometry");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility5.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility5.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility5.addAppointment(tempAppointment);
        }
        assertNotEquals(facility, facility5);
    }

    @Test
    void testPatientEquality() {
        // Different patients are not equal
        Facility facility6 = new Facility(40.70, 74.0, "Surgery");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility6.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility6.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility6.addAppointment(tempAppointment);
        }
        Patient patient3 = new Patient("Measles", "None", "Deaf");
        facility6.addPatient(patient3);
        assertNotEquals(facility, facility6);
    }

    @Test
    void testPractitionerEquality() {
        // Different practitioners are not equal
        Facility facility7 = new Facility(40.70, 74.0, "Surgery");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility7.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility7.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility7.addAppointment(tempAppointment);
        }
        MedicalPractitioner practitioner3 = new MedicalPractitioner(40.72, 74.02, "Dentistry", 75, 15);
        facility7.addPractitioner(practitioner3);
        assertNotEquals(facility, facility7);
    }

    @Test
    void testAppointmentEquality() throws ParseException {
        // Different appointments are not equal
        Facility facility8 = new Facility(40.70, 74.0, "Surgery");
        for (Iterator<Patient> it = patients.iterator(); it.hasNext(); ) {
            Patient tempPatient = it.next();
            facility8.addPatient(tempPatient);
        }
        for (Iterator<MedicalPractitioner> it = practitioners.iterator(); it.hasNext(); ) {
            MedicalPractitioner tempPractitioner = it.next();
            facility8.addPractitioner(tempPractitioner);
        }
        for (Iterator<Appointment> it = appointments.iterator(); it.hasNext(); ) {
            Appointment tempAppointment = it.next();
            facility8.addAppointment(tempAppointment);
        }
        Date date3 = formatter.parse("2023-10-22 07:15");
        Appointment appointment3 = new Appointment(date3, 50);
        facility8.addAppointment(appointment3);
        assertNotEquals(facility, facility8);
    }
}
