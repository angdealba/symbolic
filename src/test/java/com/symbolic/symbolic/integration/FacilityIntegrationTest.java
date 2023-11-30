package com.symbolic.symbolic.integration;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.*;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Provides internal integration testing between the FacilityController and 4 Repositories
 * and Entity types, along with the authentication code and the joins between Facility-Patient,
 * Facility-Practitioner, and Facility-Appointment data.
 * Provides external integration testing between the Repositories and the database implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("IntegrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FacilityIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  FacilityRepository facilityRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  AppointmentRepository appointmentRepository;

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private String tokenString;

  @Test
  @BeforeAll
  public void setupAuthentication() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

    if (JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "user", "name = 'admin'") == 0) {
      jdbcTemplate.update(
          "INSERT INTO user values (?, ?, ?, ?, ?)",
          1, null, "admin", "$2a$10$WZ.eH3iwwNHlOe80trnazeG0s3l6RFxvP5zIuk5yMTecIWNg2tXrO", "ADMIN"
      );
    }

    MvcResult result = mockMvc.perform(post("/api/client/authenticate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":  \"admin\", \"password\":  \"password\"}"))
        .andExpect(status().isOk())
        .andReturn();

    System.out.println(result.getResponse().getContentAsString());
    String responseValue = result.getResponse().getContentAsString();
    tokenString = "Bearer " + responseValue.substring(10, responseValue.length() - 2);
  }

  @AfterEach
  public void tearDownDBs() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "facility_appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "facility_patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "facility_practitioners");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioners");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "facilities");
  }

  @Test
  public void testGetAllFacilities() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Facility facility2 = new Facility(40.71, 74.01, "Optometry");

    // Test when no facilities exist
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "facilities");
    mockMvc.perform(get("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test when facilities are returned
    facilityRepository.save(facility1);
    facilityRepository.save(facility2);

    mockMvc.perform(get("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetFacilityById() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = facilityRepository.save(facility1).getId();

    // Test retrieving a facility with a valid id
    mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreateFacility() throws Exception {
    // Create valid facility
    mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isCreated());

    // Creating facilities with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'specialization' field in request body", result3.getResponse().getContentAsString());
  }

  @Test
  public void testUpdateFacility() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = facilityRepository.save(facility1).getId();

    // Test updating a facility with a valid id
    mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testDeleteFacility() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID id = facilityRepository.save(facility1).getId();

    // Test deleting a facility with a valid id
    mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(date1, 100);
    UUID patientId = patientRepository.save(patient).getId();
    id = facilityRepository.save(facility1).getId();

    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    facilityRepository.save(facility1);
    facilityRepository.save(facility2);

    // Test deleting all facilities
    mockMvc.perform(delete("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(date1, 100);
    UUID patientId = patientRepository.save(patient).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());
    assertFalse(patient.getFacilities().contains(facility1));
    assertNull(practitioner.getFacility());
    assertNull(appointment.getFacility());
  }

  @Test
  public void testGetPatientsByFacilityId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    facility1.addPatient(patient1);
    facility1.addPatient(patient2);
    UUID id = facilityRepository.save(facility1).getId();

    // Test retrieving patients
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionersByFacilityId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    facility1.addPractitioner(practitioner1);
    facility1.addPractitioner(practitioner2);
    UUID id = facilityRepository.save(facility1).getId();

    // Test retrieving practitioners
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetAppointmentsByFacilityId() throws Exception {
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = formatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    facility1.addAppointment(appointment1);
    facility1.addAppointment(appointment2);
    UUID id = facilityRepository.save(facility1).getId();

    // Test retrieving appointments
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/facility/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilitiesByPatientId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Facility facility2 = new Facility(40.71, 74.01, "Optometry");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving facilities
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/facilities")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilityByPractitionerId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving facilities
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetFacilityByAppointmentId() throws Exception {
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    Date date1 = formatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = appointmentRepository.save(appointment1).getId();

    // Test retrieving facilities
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/facility")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientFacility() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility1 = new Facility(40.7, 74.0, "Surgery");
    UUID patientId = patientRepository.save(patient1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining patient-facility
    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining practitioner-facility
    mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing practitioner or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining appointment-facility
    mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or facility ID
    MvcResult result1 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID patientId = patientRepository.save(patient1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining patient-facility
    mockMvc.perform(post("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining practitioner-facility
    mockMvc.perform(post("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test removing practitioner-facility
    mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing practitioner or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID facilityId = facilityRepository.save(facility1).getId();

    // Test joining appointment-facility
    mockMvc.perform(post("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isOk());

    // Test removing appointment-facility
    mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or facility ID
    MvcResult result1 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'facilityId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'facilityId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID facilityId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"facilityId\": \"" + facilityId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No facility found with id " + facilityId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/facility/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"facilityId\": \"" + facilityId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }
}
