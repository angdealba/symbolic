package com.symbolic.symbolic.entity;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AppointmentTest {
    private Appointment appointment;
    private Patient patient;
    private MedicalPractitioner practitioner;
    private Facility facility;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @BeforeEach
    void setup() throws ParseException {
        // Create the appointment object and its associated patient, practitioner, and facility
        Date date = formatter.parse("2023-10-20 12:00");
        this.appointment = new Appointment(date, 100);

        this.patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
        this.appointment.setPatient(this.patient);

        this.practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
        this.appointment.setPractitioner(this.practitioner);

        this.facility = new Facility(40.7, 74.0, "Surgery");
        this.appointment.setFacility(this.facility);
    }

    @Test
    void testConstructorAndGetters() throws ParseException {
        // Check equality of basic fields
        assertEquals(appointment.getDateTime(), formatter.parse("2023-10-20 12:00"));
        assertEquals(appointment.getCost(), 100);

        // Check equality of patient, practitioner, and facility objects after they are stored in the appointment
        assertEquals(appointment.getPatient(), patient);
        assertEquals(appointment.getPractitioner(), practitioner);
        assertEquals(appointment.getFacility(), facility);
    }

    @Test
    void testSetters() throws ParseException {
        // Test each setter for the different fields
        UUID id = UUID.randomUUID();
        appointment.setId(id);
        assertEquals(appointment.getId(), id);

        appointment.setDateTime(formatter.parse("2023-10-21 2:30"));
        assertEquals(appointment.getDateTime(), formatter.parse("2023-10-21 2:30"));

        appointment.setCost(200);
        assertEquals(appointment.getCost(), 200);

        // Also check the setters for patient and practitioner
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        appointment.setPatient(newPatient);
        assertEquals(appointment.getPatient(), newPatient);

        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        appointment.setPractitioner(newPractitioner);
        assertEquals(appointment.getPractitioner(), newPractitioner);

        Facility newFacility = new Facility(40.71, 74.01, "Optometry");
        appointment.setFacility(newFacility);
        assertEquals(appointment.getFacility(), newFacility);
    }

    @Test
    void testHashCode() {
        assertNotNull(appointment.hashCode());
    }

    @Test
    void testEquals() throws ParseException {
        // Base cases for testing simple equality
        assertEquals(appointment, appointment);
        assertNotEquals(appointment, "Test string");
        assertNotEquals(appointment, null);

        // Different objects with same values are equal
        Date date = formatter.parse("2023-10-20 12:00");
        Appointment appointment2 = new Appointment(date, 100);
        appointment2.setPatient(patient);
        appointment2.setPractitioner(practitioner);
        appointment2.setFacility(facility);
        assertEquals(appointment, appointment2);

        // Changes in different basic fields are not equal
        Date newDate = formatter.parse("2023-10-21 2:30");
        Appointment appointment3 = new Appointment(newDate, 100);
        appointment3.setPatient(patient);
        appointment3.setPractitioner(practitioner);
        appointment3.setFacility(facility);
        assertNotEquals(appointment, appointment3);

        Appointment appointment4 = new Appointment(date, 200);
        appointment4.setPatient(patient);
        appointment4.setPractitioner(practitioner);
        appointment4.setFacility(facility);
        assertNotEquals(appointment, appointment4);

        // Different patients are not equal
        Appointment appointment5 = new Appointment(date, 100);
        Patient newPatient = new Patient("Flu", "Tree Nut", "None");
        appointment5.setPatient(newPatient);
        appointment5.setPractitioner(practitioner);
        appointment5.setFacility(facility);
        assertNotEquals(appointment, appointment5);

        // Different practitioners are not equal
        Appointment appointment6 = new Appointment(date, 100);
        MedicalPractitioner newPractitioner = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
        appointment6.setPatient(patient);
        appointment6.setPractitioner(newPractitioner);
        appointment6.setFacility(facility);
        assertNotEquals(appointment, appointment6);

        // Different facilities are not equal
        Appointment appointment7 = new Appointment(date, 100);
        Facility newFacility = new Facility(40.71, 74.01, "Optometry");
        appointment7.setPatient(patient);
        appointment7.setPractitioner(practitioner);
        appointment7.setFacility(newFacility);
        assertNotEquals(appointment, appointment7);
    }

    @Test
    void testIDEquality() throws ParseException {
      // Different IDs are not equal
      UUID id1 = UUID.randomUUID();
      appointment.setId(id1);

      Date date = formatter.parse("2023-10-20 12:00");
      Appointment appointment2 = new Appointment(date, 100);
      appointment2.setPatient(patient);
      appointment2.setPractitioner(practitioner);
      appointment2.setFacility(facility);
      UUID id2 = UUID.randomUUID();
      appointment2.setId(id2);
      assertNotEquals(appointment, appointment2);
    }
}
