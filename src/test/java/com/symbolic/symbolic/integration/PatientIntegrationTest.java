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
import org.springframework.security.test.context.support.WithSecurityContext;
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
 * Provides internal integration testing between the PatientController and 6 Repositories
 * and Entity types, along with the authentication code and the joins between Patient-Appointment,
 * Patient-Prescription, and Patient-Diagnosis data.
 * Provides external integration testing between the Repositories and the database implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("IntegrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PatientIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  PatientRepository patientRepository;
  @Autowired
  MedicalPractitionerRepository practitionerRepository;
  @Autowired
  FacilityRepository facilityRepository;
  @Autowired
  AppointmentRepository appointmentRepository;
  @Autowired
  PrescriptionRepository prescriptionRepository;
  @Autowired
  DiagnosisRepository diagnosisRepository;
  @Autowired
  InsurancePolicyRepository insurancePolicyRepository;

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
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patient_appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patient_diagnoses");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patient_prescriptions");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "appointments");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "diagnoses");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "prescriptions");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patients");
  }

  @Test
  public void testGetAllPatients() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");

    // Test when no patients exist
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patients");
    mockMvc.perform(get("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test when patients are returned
    patientRepository.save(patient1);
    patientRepository.save(patient2);

    mockMvc.perform(get("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetPatientById() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving a patient with a valid id
    mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreatePatient() throws Exception {
    // Create valid patient
    mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"allergies\": \"Dairy\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isCreated());

    // Creating patients with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"allergies\": \"Dairy\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'vaccinations' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'allergies' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"allergies\": \"Dairy\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'accommodations' field in request body", result3.getResponse().getContentAsString());
  }

  @Test
  public void testUpdatePatient() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = patientRepository.save(patient1).getId();

    // Test updating a patient with a valid id
    mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"vaccinations\": \"Flu\", " +
                "\"allergies\": \"Tree Nut\", " +
                "\"accommodations\": \"None\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testDeletePatient() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = patientRepository.save(patient1).getId();

    // Test deleting a patient with a valid id
    mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting associated fields
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    Date aptDate1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(aptDate1, 100);
    Prescription prescription = new Prescription(1, 2, 100, "Test instructions");
    Date diaDate1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", diaDate1);
    InsurancePolicy policy = new InsurancePolicy(100);
    UUID appointmentId = appointmentRepository.save(appointment).getId();
    id = patientRepository.save(patient1).getId();

    // Test removing appointment-patient
    mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(practitioner.getPatients().contains(patient1));
    assertFalse(facility.getPatients().contains(patient1));
    assertNull(appointment.getPatient());
    assertNull(prescription.getPatient());
    assertNull(diagnosis.getPatient());
    assertFalse(policy.getPatients().contains(patient1));
  }

  @Test
  public void testDeleteAllPatients() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    patientRepository.save(patient1);
    patientRepository.save(patient2);

    // Test deleting all patients
    mockMvc.perform(delete("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test deleting associated fields
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    Date aptDate1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(aptDate1, 100);
    Prescription prescription = new Prescription(1, 2, 100, "Test instructions");
    Date diaDate1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", diaDate1);
    InsurancePolicy policy = new InsurancePolicy(100);
    UUID appointmentId = appointmentRepository.save(appointment).getId();
    UUID id = patientRepository.save(patient1).getId();

    // Test removing appointment-patient
    mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    mockMvc.perform(delete("/api/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());
    assertFalse(practitioner.getPatients().contains(patient1));
    assertFalse(facility.getPatients().contains(patient1));
    assertNull(appointment.getPatient());
    assertNull(prescription.getPatient());
    assertNull(diagnosis.getPatient());
    assertFalse(policy.getPatients().contains(patient1));
  }

  @Test
  public void testGetAppointmentsByPatientId() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = appointmentFormatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    patient1.addAppointment(appointment1);
    patient1.addAppointment(appointment2);
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving appointments
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPrescriptionsByPatientId() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    patient1.addPrescription(prescription1);
    patient1.addPrescription(prescription2);
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving prescriptions
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetDiagnosesByPatientId() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Date date2 = diagnosisFormatter.parse("2023-10-21");
    Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", date2);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    patient1.addDiagnosis(diagnosis1);
    patient1.addDiagnosis(diagnosis2);
    UUID id = patientRepository.save(patient1).getId();

    // Test retrieving diagnoses
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatientsByAppointmentId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    UUID id = appointmentRepository.save(appointment1).getId();

    // Test retrieving patients
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatientsByPrescriptionId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    UUID id = prescriptionRepository.save(prescription1).getId();

    // Test retrieving patients
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPatientsByDiagnosisId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    UUID id = diagnosisRepository.save(diagnosis1).getId();

    // Test retrieving patients
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinAppointmentPatient() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test joining appointment-patient
    mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinPrescriptionPatient() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID prescriptionId = prescriptionRepository.save(prescription1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test joining prescription-patient
    mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testAddJoinDiagnosisPatient() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID diagnosisId = diagnosisRepository.save(diagnosis1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test joining diagnosis-patient
    mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing diagnosis or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinAppointmentPatient() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID appointmentId = appointmentRepository.save(appointment1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test removing appointment-patient
    mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPrescriptionPatient() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID prescriptionId = prescriptionRepository.save(prescription1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test removing prescription-patient
    mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinDiagnosisPatient() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID diagnosisId = diagnosisRepository.save(diagnosis1).getId();
    UUID patientId = patientRepository.save(patient1).getId();

    // Test removing diagnosis-patient
    mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing diagnosis or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }
}
