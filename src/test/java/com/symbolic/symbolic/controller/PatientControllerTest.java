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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
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
public class PatientControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @MockBean
  FacilityRepository facilityRepository;
  @MockBean
  AppointmentRepository appointmentRepository;
  @MockBean
  PrescriptionRepository prescriptionRepository;
  @MockBean
  DiagnosisRepository diagnosisRepository;
  @MockBean
  InsurancePolicyRepository insurancePolicyRepository;
  @InjectMocks
  PatientController patientController;

  AutoCloseable openMocks;

  private SimpleDateFormat appointmentFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
  private SimpleDateFormat diagnosisFormatter = new SimpleDateFormat("yyyy-MM-dd");

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
    assertEquals(id, PatientController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(PatientController.parseUuidFromString("test"));
    assertNull(PatientController.parseUuidFromString("2"));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetAllPatients() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    List<Patient> patients = new ArrayList<>();
    when(patientRepository.findAll()).thenReturn(patients);

    // Test when no patients exist
    mockMvc.perform(get("/api/patients")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test when patients are returned
    patients.add(patient1);
    patients.add(patient2);

    mockMvc.perform(get("/api/patients")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "VACCINATION_RECORD_APP")
  public void testGetPatientById() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    patient1.setId(id);
    when(patientRepository.findById(id)).thenReturn(Optional.of(patient1));

    // Test retrieving a patient with a valid id
    mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testCreatePatient() throws Exception {
    // Create valid patient
    mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"allergies\": \"Dairy\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isCreated());

    // Creating patients with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"allergies\": \"Dairy\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'vaccinations' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"accommodations\": \"Wheelchair Access\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'allergies' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"vaccinations\": \"COVID-19\", " +
                "\"allergies\": \"Dairy\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'accommodations' field in request body", result3.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testUpdatePatient() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    patient1.setId(id);
    when(patientRepository.findById(id)).thenReturn(Optional.of(patient1));

    // Test updating a patient with a valid id
    mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"vaccinations\": \"Flu\", " +
                "\"allergies\": \"Tree Nut\", " +
                "\"accommodations\": \"None\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeletePatient() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    patient1.setId(id);
    when(patientRepository.findById(id)).thenReturn(Optional.of(patient1));

    // Test deleting a patient with a valid id
    mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
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

    patient1.addPractitioner(practitioner);
    patient1.addFacility(facility);
    patient1.addAppointment(appointment);
    patient1.addPrescription(prescription);
    patient1.addDiagnosis(diagnosis);
    patient1.setInsurancePolicy(policy);

    mockMvc.perform(delete("/api/patient")
            .contentType(MediaType.APPLICATION_JSON)
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
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeleteAllPatients() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    List<Patient> patients = new ArrayList<>();

    patients.add(patient1);
    patients.add(patient2);
    when(patientRepository.findAll()).thenReturn(patients);

    // Test deleting all patients
    mockMvc.perform(delete("/api/patients")
            .contentType(MediaType.APPLICATION_JSON))
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

    patient1.addPractitioner(practitioner);
    patient1.addFacility(facility);
    patient1.addAppointment(appointment);
    patient1.addPrescription(prescription);
    patient1.addDiagnosis(diagnosis);
    patient1.setInsurancePolicy(policy);

    mockMvc.perform(delete("/api/patients")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    assertFalse(practitioner.getPatients().contains(patient1));
    assertFalse(facility.getPatients().contains(patient1));
    assertNull(appointment.getPatient());
    assertNull(prescription.getPatient());
    assertNull(diagnosis.getPatient());
    assertFalse(policy.getPatients().contains(patient1));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetAppointmentsByPatientId() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = appointmentFormatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    List<Appointment> appointments = new ArrayList<>();
    appointments.add(appointment1);
    appointments.add(appointment2);
    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(appointmentRepository.findAppointmentsByPatientId(id)).thenReturn(appointments);

    // Test retrieving appointments
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPrescriptionsByPatientId() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
    List<Prescription> prescriptions = new ArrayList<>();
    prescriptions.add(prescription1);
    prescriptions.add(prescription2);
    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(prescriptionRepository.findPrescriptionsByPatientId(id)).thenReturn(prescriptions);

    // Test retrieving prescriptions
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "VACCINATION_RECORD_APP")
  public void testGetDiagnosesByPatientId() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Date date2 = diagnosisFormatter.parse("2023-10-21");
    Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", date2);
    List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.add(diagnosis1);
    diagnoses.add(diagnosis2);
    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(diagnosisRepository.findDiagnosesByPatientId(id)).thenReturn(diagnoses);

    // Test retrieving diagnoses
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPatientsByAppointmentId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    when(appointmentRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientByAppointmentsId(id)).thenReturn(patient1);

    // Test retrieving patients
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPatientsByPrescriptionId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    when(prescriptionRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientByPrescriptionsId(id)).thenReturn(patient1);

    // Test retrieving patients
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/prescription/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPatientsByDiagnosisId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID id = UUID.randomUUID();
    when(diagnosisRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientByDiagnosesId(id)).thenReturn(patient1);

    // Test retrieving patients
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/diagnosis/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testAddJoinAppointmentPatient() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID appointmentId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment1));
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test joining appointment-patient
    mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old patient
    when(patientRepository.findPatientByAppointmentsId(appointmentId)).thenReturn(patient1);
    mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testAddJoinPrescriptionPatient() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID prescriptionId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription1));
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test joining prescription-patient
    mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old patient
    when(patientRepository.findPatientByPrescriptionsId(prescriptionId)).thenReturn(patient1);
    mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testAddJoinDiagnosisPatient() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID diagnosisId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis1));
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test joining diagnosis-patient
    mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old patient
    when(patientRepository.findPatientByDiagnosesId(diagnosisId)).thenReturn(patient1);
    mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk());

    // Test missing diagnosis or patient ID
    MvcResult result1 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testRemoveJoinAppointmentPatient() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID appointmentId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(appointmentRepository.existsById(appointmentId)).thenReturn(true);
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test removing appointment-patient
    mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No appointment found with id " + appointmentId2, result6.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testRemoveJoinPrescriptionPatient() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID prescriptionId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test removing prescription-patient
    mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + prescriptionId2, result6.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testRemoveJoinDiagnosisPatient() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    UUID diagnosisId = UUID.randomUUID();
    UUID patientId = UUID.randomUUID();

    when(diagnosisRepository.existsById(diagnosisId)).thenReturn(true);
    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));

    // Test removing diagnosis-patient
    mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing diagnosis or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"2\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID patientId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"patientId\": \"" + patientId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/patient/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }
}
