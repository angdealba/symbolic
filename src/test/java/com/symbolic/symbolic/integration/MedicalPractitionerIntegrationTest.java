package com.symbolic.symbolic.integration;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.*;
import com.symbolic.symbolic.service.MedicalPractitionerService;
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
 * Provides internal integration testing between the MedicalPractitionerController and 6 Repositories
 * and Entity types, along with the authentication code and the joins between Patient-Appointment,
 * Patient-Prescription, and Patient-Diagnosis data.  Also provides internal integration testing
 * between the MedicalPractitionerController and MedicalPractitionerService.
 * Provides external integration testing between the Repositories and the database implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("IntegrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MedicalPractitionerIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  FacilityRepository facilityRepository;
  @Autowired
  AppointmentRepository appointmentRepository;
  @Autowired
  PrescriptionRepository prescriptionRepository;
  @Autowired
  DiagnosisRepository diagnosisRepository;
  @Autowired
  MedicalPractitionerService medicalPractitionerService;
  private SimpleDateFormat appointmentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private SimpleDateFormat diagnosisFormatter = new SimpleDateFormat("yyyy-MM-dd");

  private String tokenString;

  @Test
  @BeforeAll
  public void setupAuthentication() throws Exception {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

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
  @AfterAll
  public void tearDownDBs() {
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioner_appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioner_diagnoses");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioner_prescriptions");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioner_patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "diagnoses");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "prescriptions");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioners");
  }

  /**
   * Internal integration test between MedicalPractitionerController and MedicalPractitionerService.
   */
  @Test
  public void testSearch() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Surgery", 100, 20);
    practitionerRepository.save(practitioner1);
    practitionerRepository.save(practitioner2);

    // Test valid return
    mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isOk());

    // Test valid return with nullable parameters
    mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\"}"))
        .andExpect(status().isOk());

    // Test missing latitude and longitude fields
    MvcResult result1 = mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());
  }

  @Test
  public void testGetAllPractitioners() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);

    // Test when no practitioners exist
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "practitioners");
    mockMvc.perform(get("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test when practitioners are returned
    practitionerRepository.save(practitioner1);
    practitionerRepository.save(practitioner2);

    mockMvc.perform(get("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetPractitionerById() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving a practitioner with a valid id
    mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreatePractitioner() throws Exception {
    // Create valid practitioner
    mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isCreated());

    // Creating practitioners with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'specialization' field in request body", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'consultationCost' field in request body", result4.getResponse().getContentAsString());

    MvcResult result5 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'yearsExperience' field in request body", result5.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result6 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\", " +
                "\"consultationCost\": \"-1\", " +
                "\"yearsExperience\": \"15\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'consultationCost' field must be a non-negative integer", result6.getResponse().getContentAsString());

    MvcResult result7 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"-1\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'yearsExperience' field must be a non-negative integer", result7.getResponse().getContentAsString());
  }

  @Test
  public void testUpdatePractitioner() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test updating a practitioner with a valid id
    mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"15\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test updating with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + id2, result3.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result4 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\", " +
                "\"consultationCost\": \"-1\", " +
                "\"yearsExperience\": \"15\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'consultationCost' field must be a non-negative integer", result4.getResponse().getContentAsString());

    MvcResult result5 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"latitude\": \"40.71\", " +
                "\"longitude\": \"74.01\", " +
                "\"specialization\": \"Optometry\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"-1\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'yearsExperience' field must be a non-negative integer", result5.getResponse().getContentAsString());
  }

  @Test
  public void testDeletePractitioner() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test deleting a practitioner with a valid id
    mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    Date aptDate1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(aptDate1, 100);
    Prescription prescription = new Prescription(1, 2, 100, "Test instructions");
    Date diaDate1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", diaDate1);
    UUID patientId = patientRepository.save(patient).getId();
    id = practitionerRepository.save(practitioner1).getId();

    // Test joining patient-practitioner
    mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(patient.getPractitioners().contains(practitioner1));
    assertFalse(facility.getPractitioners().contains(practitioner1));
    assertNull(appointment.getPractitioner());
    assertNull(prescription.getPractitioner());
    assertNull(diagnosis.getPractitioner());
  }

  @Test
  public void testDeleteAllPractitioners() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    practitionerRepository.save(practitioner1);
    practitionerRepository.save(practitioner2);

    // Test deleting all practitioners
    mockMvc.perform(delete("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    Date aptDate1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(aptDate1, 100);
    Prescription prescription = new Prescription(1, 2, 100, "Test instructions");
    Date diaDate1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", diaDate1);
    UUID patientId = patientRepository.save(patient).getId();
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test joining patient-practitioner
    mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());
    assertFalse(patient.getPractitioners().contains(practitioner1));
    assertFalse(facility.getPractitioners().contains(practitioner1));
    assertNull(appointment.getPractitioner());
    assertNull(prescription.getPractitioner());
    assertNull(diagnosis.getPractitioner());
  }

  @Test
  public void testGetPatientsByPractitionerId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    practitioner1.addPatient(patient1);
    practitioner1.addPatient(patient2);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving patients
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetAppointmentsByPractitionerId() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = appointmentFormatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    practitioner1.addAppointment(appointment1);
    practitioner1.addAppointment(appointment2);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving appointments
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPrescriptionsByPractitionerId() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    practitioner1.addPrescription(prescription1);
    practitioner1.addPrescription(prescription2);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving prescriptions
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetDiagnosesByPatientId() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Date date2 = diagnosisFormatter.parse("2023-10-21");
    Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", date2);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    practitioner1.addDiagnosis(diagnosis1);
    practitioner1.addDiagnosis(diagnosis2);
    UUID id = practitionerRepository.save(practitioner1).getId();

    // Test retrieving diagnoses
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionersByPatientId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving practitioners
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByAppointmentId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = appointmentRepository.save(appointment1).getId();

    // Test retrieving practitioner
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByPrescriptionId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    UUID id = prescriptionRepository.save(prescription1).getId();

    // Test retrieving practitioner
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByDiagnosisId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    UUID id = diagnosisRepository.save(diagnosis1).getId();

    // Test retrieving practitioner
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientPractitioner() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID patientId = patientRepository.save(patient1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test joining patient-practitioner
    mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinAppointmentPractitioner() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test joining appointment-practitioner
    mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinPrescriptionPractitioner() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID prescriptionId = prescriptionRepository.save(prescription1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test joining prescription-practitioner
    mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing prescription or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinDiagnosisPractitioner() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID diagnosisId = diagnosisRepository.save(diagnosis1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test joining diagnosis-practitioner
    mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing diagnosis or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPatientPractitioner() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID patientId = patientRepository.save(patient1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test removing patient-practitioner
    mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing patient or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinAppointmentPractitioner() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test removing appointment-practitioner
    mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPrescriptionPractitioner() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID prescriptionId = prescriptionRepository.save(prescription1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test removing prescription-practitioner
    mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinDiagnosisPractitioner() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID diagnosisId = diagnosisRepository.save(diagnosis1).getId();
    UUID practitionerId = practitionerRepository.save(practitioner1).getId();

    // Test removing diagnosis-practitioner
    mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing diagnosis or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }
}
