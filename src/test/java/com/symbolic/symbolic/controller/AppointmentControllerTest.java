package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  AppointmentRepository appointmentRepository;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @MockBean
  FacilityRepository facilityRepository;
  @InjectMocks
  AppointmentController appointmentController;

  AutoCloseable openMocks;

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  @BeforeEach
  public void setUp() {
    openMocks = MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void tearDown() throws Exception {
    openMocks.close();
  }

  @Test
  public void testUUIDParser() {
    // Test valid ID
    UUID id = UUID.randomUUID();
    String idString = id.toString();
    assertEquals(id, AppointmentController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(AppointmentController.parseUuidFromString("test"));
    assertNull(AppointmentController.parseUuidFromString("2"));
  }

  @Test
  public void testGetAllAppointments() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = formatter.parse("2023-11-21 13:30");
    Appointment appointment2 = new Appointment(date2, 50);
    List<Appointment> appointments = new ArrayList<>();
    when(appointmentRepository.findAll()).thenReturn(appointments);

    // Test when no appointments exist
    mockMvc.perform(get("/api/appointments")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());


    // Test when prescriptions are returned
    appointments.add(appointment1);
    appointments.add(appointment2);

    mockMvc.perform(get("/api/appointments")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetAppointmentById() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = UUID.randomUUID();
    appointment1.setId(id);
    when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment1));

    // Test retrieving a appointment with a valid id
    mockMvc.perform(get("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreateAppointment() throws Exception {
    // Create valid appointment
    mockMvc.perform(post("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dateTime\": \"2023-10-20 12:00\", " +
                "\"cost\": \"100\"}"))
        .andExpect(status().isCreated());

    // Creating appointments with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"cost\": \"100\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'dateTime' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dateTime\": \"2023-10-20 12:00\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'cost' field in request body", result2.getResponse().getContentAsString());

    // Test malformed date error cases
    MvcResult result3 = mockMvc.perform(post("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dateTime\": \"2023-10-20\", " +
                "\"cost\": \"100\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dateTime' field value must be in the format yyyy-MM-dd HH:mm", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dateTime\": \"2023-10-20 1a:00\", " +
                "\"cost\": \"100\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dateTime' field value must be in the format yyyy-MM-dd HH:mm", result4.getResponse().getContentAsString());
  }

  @Test
  public void testUpdateAppointment() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = UUID.randomUUID();
    appointment1.setId(id);
    when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment1));

    // Test updating a appointment with a valid id
    mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dateTime\": \"2023-11-21 13:30\", " +
                "\"cost\": \"50\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + id2, result3.getResponse().getContentAsString());

    // Test updating with a malformed date raises an error
    MvcResult result4 = mockMvc.perform(put("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dateTime\": \"2023-11-21 1a:30\", " +
                "\"cost\": \"50\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dateTime' field value must be in the format yyyy-MM-dd HH:mm", result4.getResponse().getContentAsString());
  }

  @Test
  public void testDeleteAppointment() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = UUID.randomUUID();
    appointment1.setId(id);
    when(appointmentRepository.findById(id)).thenReturn(Optional.of(appointment1));

    // Test deleting a appointment with a valid id
    mockMvc.perform(delete("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting patient, practitioner, and facility fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    appointment1.setPatient(patient);
    appointment1.setPractitioner(practitioner);
    appointment1.setFacility(facility);
    mockMvc.perform(delete("/api/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(patient.getAppointments().contains(appointment1));
    assertFalse(practitioner.getAppointments().contains(appointment1));
    assertFalse(facility.getAppointments().contains(appointment1));
  }

  @Test
  public void testDeleteAllAppointments() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = formatter.parse("2023-11-21 13:30");
    Appointment appointment2 = new Appointment(date2, 50);
    List<Appointment> appointments = new ArrayList<>();
    when(appointmentRepository.findAll()).thenReturn(appointments);
    appointments.add(appointment1);
    appointments.add(appointment2);
    when(appointmentRepository.findAll()).thenReturn(appointments);

    // Test deleting all diagnoses
    mockMvc.perform(delete("/api/appointments")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting patient, practitioner, and facility fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    appointment1.setPatient(patient);
    appointment1.setPractitioner(practitioner);
    appointment1.setFacility(facility);

    mockMvc.perform(delete("/api/appointments")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }
}
