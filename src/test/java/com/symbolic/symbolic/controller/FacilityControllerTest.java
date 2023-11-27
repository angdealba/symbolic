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
public class FacilityControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  FacilityRepository facilityRepository;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  AppointmentRepository appointmentRepository;
  @InjectMocks
  FacilityController facilityController;

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
    assertEquals(id, FacilityController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(FacilityController.parseUuidFromString("test"));
    assertNull(FacilityController.parseUuidFromString("2"));
  }

  @Test
  public void testGetAllFacilities() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Facility facility2 = new Facility(40.71, 74.01, "Optometry");
    List<Facility> facilities = new ArrayList<>();
    when(facilityRepository.findAll()).thenReturn(facilities);

    // Test when no facilities exist
    mockMvc.perform(get("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test when facilities are returned
    facilities.add(facility1);
    facilities.add(facility2);

    mockMvc.perform(get("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetFacilityById() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = UUID.randomUUID();
    facility1.setId(id);
    when(facilityRepository.findById(id)).thenReturn(Optional.of(facility1));

    // Test retrieving a facility with a valid id
    mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreateFacility() throws Exception {
    // Create valid policy
    mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isCreated());

    // Creating facilities with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'specialization' field in request body", result3.getResponse().getContentAsString());
  }

  @Test
  public void testUpdateFacility() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = UUID.randomUUID();
    facility1.setId(id);
    when(facilityRepository.findById(id)).thenReturn(Optional.of(facility1));

    // Test updating a facility with a valid id
    mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testDeleteFacility() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = UUID.randomUUID();
    facility1.setId(id);
    when(facilityRepository.findById(id)).thenReturn(Optional.of(facility1));

    // Test deleting a policy with a valid id
    mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(date1, 100);
    facility1.addPatient(patient);
    facility1.addPractitioner(practitioner);
    facility1.addAppointment(appointment);

    mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(patient.getFacilities().contains(facility1));
    assertNull(practitioner.getFacility());
    assertNull(appointment.getFacility());
  }

  @Test
  public void testDeleteAllFacilities() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Facility facility2 = new Facility(40.71, 74.01, "Optometry");
    List<Facility> facilities = new ArrayList<>();
    when(facilityRepository.findAll()).thenReturn(facilities);

    facilities.add(facility1);
    facilities.add(facility2);
    when(facilityRepository.findAll()).thenReturn(facilities);

    // Test deleting all diagnoses
    mockMvc.perform(delete("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(date1, 100);
    facility1.addPatient(patient);
    facility1.addPractitioner(practitioner);
    facility1.addAppointment(appointment);

    mockMvc.perform(delete("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    assertFalse(patient.getFacilities().contains(facility1));
    assertNull(practitioner.getFacility());
    assertNull(appointment.getFacility());
  }

  @Test
  public void testGetPatientsByFacilityId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    List<Patient> patients = new ArrayList<>();
    patients.add(patient1);
    patients.add(patient2);
    UUID id = UUID.randomUUID();
    when(facilityRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientsByFacilitiesId(id)).thenReturn(patients);

    // Test retrieving patients
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionersByFacilityId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    practitioners.add(practitioner1);
    practitioners.add(practitioner2);
    UUID id = UUID.randomUUID();
    when(facilityRepository.existsById(id)).thenReturn(true);
    when(practitionerRepository.findMedicalPractitionerByFacilityId(id)).thenReturn(practitioners);

    // Test retrieving patients
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetAppointmentsByFacilityId() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = formatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    List<Appointment> appointments = new ArrayList<>();
    appointments.add(appointment1);
    appointments.add(appointment2);
    UUID id = UUID.randomUUID();
    when(facilityRepository.existsById(id)).thenReturn(true);
    when(appointmentRepository.findAppointmentsByFacilityId(id)).thenReturn(appointments);

    // Test retrieving patients
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilitiesByPatientId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Facility facility2 = new Facility(40.71, 74.01, "Optometry");
    List<Facility> facilities = new ArrayList<>();

    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(facilityRepository.findFacilitiesByPatientsId(id)).thenReturn(facilities);

    // Test retrieving facilities
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilityByPractitionerId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = UUID.randomUUID();
    when(practitionerRepository.existsById(id)).thenReturn(true);
    when(facilityRepository.findFacilityByPractitionersId(id)).thenReturn(facility1);

    // Test retrieving facilities
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilityByAppointmentId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = UUID.randomUUID();
    when(appointmentRepository.existsById(id)).thenReturn(true);
    when(facilityRepository.findFacilityByAppointmentsId(id)).thenReturn(facility1);

    // Test retrieving facilities
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientFacility() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID patientId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test joining patient-facility
    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old facility
    List<Facility> facilities = new ArrayList<>();
    facilities.add(facility1);
    when(facilityRepository.findFacilitiesByPatientsId(patientId)).thenReturn(facilities);
    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinPractitionerFacility() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID practitionerId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test joining practitioner-facility
    mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old facility
    when(facilityRepository.findFacilityByPractitionersId(practitionerId)).thenReturn(facility1);
    mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing practitioner or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinAppointmentFacility() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID appointmentId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment1));
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test joining appointment-facility
    mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old facility
    when(facilityRepository.findFacilityByAppointmentsId(appointmentId)).thenReturn(facility1);
    mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPatientFacility() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID patientId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(patientRepository.existsById(patientId)).thenReturn(true);
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test removing patient-facility
    mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing patient or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPractitionerFacility() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID practitionerId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(practitionerRepository.existsById(practitionerId)).thenReturn(true);
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test removing practitioner-facility
    mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing practitioner or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinAppointmentFacility() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID appointmentId = UUID.randomUUID();
    UUID facilityId = UUID.randomUUID();

    when(appointmentRepository.existsById(appointmentId)).thenReturn(true);
    when(facilityRepository.findById(facilityId)).thenReturn(Optional.of(facility1));

    // Test removing appointment-facility
    mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }
}
