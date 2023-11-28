package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.*;
import com.symbolic.symbolic.repository.*;
import com.symbolic.symbolic.service.MedicalPractitionerService;
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
public class MedicalPractitionerControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  FacilityRepository facilityRepository;
  @MockBean
  AppointmentRepository appointmentRepository;
  @MockBean
  PrescriptionRepository prescriptionRepository;
  @MockBean
  DiagnosisRepository diagnosisRepository;
  @InjectMocks
  MedicalPractitionerController practitionerController;
  @InjectMocks
  MedicalPractitionerService medicalPractitionerService;

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
    assertEquals(id, MedicalPractitionerController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(MedicalPractitionerController.parseUuidFromString("test"));
    assertNull(MedicalPractitionerController.parseUuidFromString("2"));
  }

  @Test
  public void testGetAllPractitioners() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    when(practitionerRepository.findAll()).thenReturn(practitioners);

    // Test when no practitioners exist
    mockMvc.perform(get("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test when practitioners are returned
    practitioners.add(practitioner1);
    practitioners.add(practitioner2);

    mockMvc.perform(get("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetPractitionerById() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = UUID.randomUUID();
    practitioner1.setId(id);
    when(practitionerRepository.findById(id)).thenReturn(Optional.of(practitioner1));

    // Test retrieving a practitioner with a valid id
    mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isCreated());

    // Creating practitioners with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"consultationCost\": \"50\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'specialization' field in request body", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'consultationCost' field in request body", result4.getResponse().getContentAsString());

    MvcResult result5 = mockMvc.perform(post("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID id = UUID.randomUUID();
    practitioner1.setId(id);
    when(practitionerRepository.findById(id)).thenReturn(Optional.of(practitioner1));

    // Test updating a practitioner with a valid id
    mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test updating with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + id2, result3.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result4 = mockMvc.perform(put("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID id = UUID.randomUUID();
    practitioner1.setId(id);
    when(practitionerRepository.findById(id)).thenReturn(Optional.of(practitioner1));

    // Test deleting a practitioner with a valid id
    mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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

    practitioner1.addPatient(patient);
    practitioner1.setFacility(facility);
    practitioner1.addAppointment(appointment);
    practitioner1.addPrescription(prescription);
    practitioner1.addDiagnosis(diagnosis);

    mockMvc.perform(delete("/api/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
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
    List<MedicalPractitioner> practitioners = new ArrayList<>();

    practitioners.add(practitioner1);
    practitioners.add(practitioner2);
    when(practitionerRepository.findAll()).thenReturn(practitioners);

    // Test deleting all practitioners
    mockMvc.perform(delete("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting associated fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Facility facility = new Facility(40.7, 74.0, "Surgery");
    Date aptDate1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment = new Appointment(aptDate1, 100);
    Prescription prescription = new Prescription(1, 2, 100, "Test instructions");
    Date diaDate1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis = new Diagnosis("COVID-19", "Antiviral Medication", diaDate1);

    practitioner1.addPatient(patient);
    practitioner1.setFacility(facility);
    practitioner1.addAppointment(appointment);
    practitioner1.addPrescription(prescription);
    practitioner1.addDiagnosis(diagnosis);

    mockMvc.perform(delete("/api/practitioners")
            .contentType(MediaType.APPLICATION_JSON))
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
    List<Patient> patients = new ArrayList<>();
    patients.add(patient1);
    patients.add(patient2);
    UUID id = UUID.randomUUID();
    when(practitionerRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientsByPractitionersId(id)).thenReturn(patients);

    // Test retrieving patients
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetAppointmentsByPractitionerId() throws Exception {
    Date date1 = appointmentFormatter.parse("2023-10-20 12:00");
    Appointment appointment1 = new Appointment(date1, 100);
    Date date2 = appointmentFormatter.parse("2023-10-21 2:30");
    Appointment appointment2 = new Appointment(date2, 200);
    List<Appointment> appointments = new ArrayList<>();
    appointments.add(appointment1);
    appointments.add(appointment2);
    UUID id = UUID.randomUUID();
    when(practitionerRepository.existsById(id)).thenReturn(true);
    when(appointmentRepository.findAppointmentsByPractitionerId(id)).thenReturn(appointments);

    // Test retrieving appointments
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/appointments")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPrescriptionsByPractitionerId() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 1, 50, "More test instructions");
    List<Prescription> prescriptions = new ArrayList<>();
    prescriptions.add(prescription1);
    prescriptions.add(prescription2);
    UUID id = UUID.randomUUID();
    when(practitionerRepository.existsById(id)).thenReturn(true);
    when(prescriptionRepository.findPrescriptionsByPractitionerId(id)).thenReturn(prescriptions);

    // Test retrieving prescriptions
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/prescriptions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetDiagnosesByPatientId() throws Exception {
    Date date1 = diagnosisFormatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "Antiviral Medication", date1);
    Date date2 = diagnosisFormatter.parse("2023-10-21");
    Diagnosis diagnosis2 = new Diagnosis("COVID-19", "In-Patient Treatment", date2);
    List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.add(diagnosis1);
    diagnoses.add(diagnosis2);
    UUID id = UUID.randomUUID();
    when(practitionerRepository.existsById(id)).thenReturn(true);
    when(diagnosisRepository.findDiagnosesByPractitionerId(id)).thenReturn(diagnoses);

    // Test retrieving diagnoses
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/practitioner/diagnoses")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionersByPatientId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Optometry", 100, 20);
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(practitionerRepository.findMedicalPractitionerByPatientsId(id)).thenReturn(practitioners);

    // Test retrieving practitioners
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/practitioners")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByAppointmentId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = UUID.randomUUID();
    when(appointmentRepository.existsById(id)).thenReturn(true);
    when(practitionerRepository.findMedicalPractitionerByAppointmentsId(id)).thenReturn(practitioner1);

    // Test retrieving practitioner
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/appointment/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByPrescriptionId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = UUID.randomUUID();
    when(prescriptionRepository.existsById(id)).thenReturn(true);
    when(practitionerRepository.findMedicalPractitionerByPrescriptionsId(id)).thenReturn(practitioner1);

    // Test retrieving practitioner
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/prescription/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPractitionerByDiagnosisId() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID id = UUID.randomUUID();
    when(diagnosisRepository.existsById(id)).thenReturn(true);
    when(practitionerRepository.findMedicalPractitionerByDiagnosesId(id)).thenReturn(practitioner1);

    // Test retrieving practitioner
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/diagnosis/practitioner")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientPractitioner() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    UUID patientId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test joining patient-practitioner
    mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old practitioner
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    practitioners.add(practitioner1);
    when(practitionerRepository.findMedicalPractitionerByPatientsId(patientId)).thenReturn(practitioners);
    mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID appointmentId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment1));
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test joining appointment-practitioner
    mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old practitioner
    when(practitionerRepository.findMedicalPractitionerByAppointmentsId(appointmentId)).thenReturn(practitioner1);
    mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing appointment or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID prescriptionId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription1));
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test joining prescription-practitioner
    mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old practitioner
    when(practitionerRepository.findMedicalPractitionerByPrescriptionsId(prescriptionId)).thenReturn(practitioner1);
    mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing prescription or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID diagnosisId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(diagnosisRepository.findById(diagnosisId)).thenReturn(Optional.of(diagnosis1));
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test joining diagnosis-practitioner
    mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old practitioner
    when(practitionerRepository.findMedicalPractitionerByDiagnosesId(diagnosisId)).thenReturn(practitioner1);
    mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isOk());

    // Test missing diagnosis or practitioner ID
    MvcResult result1 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID patientId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(patientRepository.existsById(patientId)).thenReturn(true);
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test removing patient-practitioner
    mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing patient or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/patient")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID appointmentId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(appointmentRepository.existsById(appointmentId)).thenReturn(true);
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test removing appointment-practitioner
    mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing appointment or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'appointmentId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'appointmentId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"appointmentId\": \"" + appointmentId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID appointmentId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/appointment")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID prescriptionId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(prescriptionRepository.existsById(prescriptionId)).thenReturn(true);
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test removing prescription-practitioner
    mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing prescription or patient ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'prescriptionId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'prescriptionId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"prescriptionId\": \"" + prescriptionId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID prescriptionId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/prescription")
            .contentType(MediaType.APPLICATION_JSON)
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
    UUID diagnosisId = UUID.randomUUID();
    UUID practitionerId = UUID.randomUUID();

    when(diagnosisRepository.existsById(diagnosisId)).thenReturn(true);
    when(practitionerRepository.findById(practitionerId)).thenReturn(Optional.of(practitioner1));

    // Test removing diagnosis-practitioner
    mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing diagnosis or practitioner ID
    MvcResult result1 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'practitionerId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'diagnosisId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"2\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'diagnosisId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'practitionerId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID practitionerId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId + "\", " +
                "\"practitionerId\": \"" + practitionerId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No medical practitioner found with id " + practitionerId2, result5.getResponse().getContentAsString());

    UUID diagnosisId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/practitioner/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"diagnosisId\": \"" + diagnosisId2 + "\", " +
                "\"practitionerId\": \"" + practitionerId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + diagnosisId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testSearch() throws Exception {
    MedicalPractitioner practitioner1 = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    MedicalPractitioner practitioner2 = new MedicalPractitioner(40.71, 74.01, "Surgery", 100, 20);
    List<MedicalPractitioner> practitioners = new ArrayList<>();
    when(medicalPractitionerService.search(40.7, 74.0, "Surgery", 100, 10)).thenReturn(practitioners);

    // Test valid return
    mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isOk());

    // Test valid return with nullable parameters
    mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"longitude\": \"74.0\"}"))
        .andExpect(status().isOk());

    // Test missing latitude and longitude fields
    MvcResult result1 = mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"longitude\": \"74.0\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'latitude' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(get("/api/practitioners/search")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"latitude\": \"40.7\", " +
                "\"specialization\": \"Surgery\", " +
                "\"consultationCost\": \"100\", " +
                "\"yearsExperience\": \"10\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'longitude' field in request body", result2.getResponse().getContentAsString());
  }
}
