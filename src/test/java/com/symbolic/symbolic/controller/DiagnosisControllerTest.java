package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.repository.MedicalPractitionerRepository;
import com.symbolic.symbolic.repository.PatientRepository;
import com.symbolic.symbolic.repository.PrescriptionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
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
@Tag("UnitTest")
public class DiagnosisControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  DiagnosisRepository diagnosisRepository;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @InjectMocks
  DiagnosisController diagnosisController;

  AutoCloseable openMocks;

  private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

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
    assertEquals(id, DiagnosisController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(DiagnosisController.parseUuidFromString("test"));
    assertNull(DiagnosisController.parseUuidFromString("2"));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetAllDiagnoses() throws Exception {
    Date date1 = formatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "In-Patient Treatment", date1);
    Date date2 = formatter.parse("2023-11-21");
    Diagnosis diagnosis2 = new Diagnosis("Influenza", "Antiviral Medication", date2);
    List<Diagnosis> diagnoses = new ArrayList<>();
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);

    // Test when no diagnoses exist
    mockMvc.perform(get("/api/diagnoses")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());


    // Test when diagnoses are returned
    diagnoses.add(diagnosis1);
    diagnoses.add(diagnosis2);

    mockMvc.perform(get("/api/diagnoses")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "VACCINATION_RECORD_APP")
  public void testGetDiagnosisById() throws Exception {
    Date date1 = formatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "In-Patient Treatment", date1);
    UUID id = UUID.randomUUID();
    diagnosis1.setId(id);
    when(diagnosisRepository.findById(id)).thenReturn(Optional.of(diagnosis1));

    // Test retrieving a diagnosis with a valid id
    mockMvc.perform(get("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testCreateDiagnosis() throws Exception {
    // Create valid diagnosis
    mockMvc.perform(post("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\": \"2023-10-20\", " +
                "\"condition\": \"COVID-19\", " +
                "\"treatmentInfo\": \"In-Patient Treatment\"}"))
        .andExpect(status().isCreated());

    // Creating diagnoses with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"condition\": \"COVID-19\", " +
                "\"treatmentInfo\": \"In-Patient Treatment\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'date' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\": \"2023-10-20\", " +
                "\"treatmentInfo\": \"In-Patient Treatment\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'condition' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\": \"2023-10-20\", " +
                "\"condition\": \"COVID-19\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'treatmentInfo' field in request body", result3.getResponse().getContentAsString());

    // Test malformed date error case
    MvcResult result4 = mockMvc.perform(post("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"date\": \"2023-1a-21\", " +
                "\"condition\": \"COVID-19\", " +
                "\"treatmentInfo\": \"In-Patient Treatment\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'date' field value must be in the format yyyy-MM-dd", result4.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testUpdateDiagnosis() throws Exception {
    Date date1 = formatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "In-Patient Treatment", date1);
    UUID id = UUID.randomUUID();
    diagnosis1.setId(id);
    when(diagnosisRepository.findById(id)).thenReturn(Optional.of(diagnosis1));

    // Test updating a diagnosis with a valid id
    mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"date\": \"2023-11-21\", " +
                "\"condition\": \"Influenza\", " +
                "\"treatmentInfo\": \"Antiviral Medication\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + id2, result3.getResponse().getContentAsString());

    // Test updating with a malformed date raises an error
    MvcResult result4 = mockMvc.perform(put("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"date\": \"2023-11a-21\", " +
                "\"condition\": \"Influenza\", " +
                "\"treatmentInfo\": \"Antiviral Medication\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'date' field value must be in the format yyyy-MM-dd", result4.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeleteDiagnosis() throws Exception {
    Date date1 = formatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "In-Patient Treatment", date1);
    UUID id = UUID.randomUUID();
    diagnosis1.setId(id);
    when(diagnosisRepository.findById(id)).thenReturn(Optional.of(diagnosis1));

    // Test deleting a diagnosis with a valid id
    mockMvc.perform(delete("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No diagnosis found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting patient and practitioner fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    diagnosis1.setPatient(patient);
    diagnosis1.setPractitioner(practitioner);
    mockMvc.perform(delete("/api/diagnosis")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(patient.getDiagnoses().contains(diagnosis1));
    assertFalse(practitioner.getDiagnoses().contains(diagnosis1));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeleteAllDiagnoses() throws Exception {
    Date date1 = formatter.parse("2023-10-20");
    Diagnosis diagnosis1 = new Diagnosis("COVID-19", "In-Patient Treatment", date1);
    Date date2 = formatter.parse("2023-11-21");
    Diagnosis diagnosis2 = new Diagnosis("Influenza", "Antiviral Medication", date2);
    List<Diagnosis> diagnoses = new ArrayList<>();
    diagnoses.add(diagnosis1);
    diagnoses.add(diagnosis2);
    when(diagnosisRepository.findAll()).thenReturn(diagnoses);

    // Test deleting all diagnoses
    mockMvc.perform(delete("/api/diagnoses")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting patient and practitioner fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    diagnosis1.setPatient(patient);
    diagnosis1.setPractitioner(practitioner);

    mockMvc.perform(delete("/api/diagnoses")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    assertFalse(patient.getDiagnoses().contains(diagnosis1));
    assertFalse(practitioner.getDiagnoses().contains(diagnosis1));
  }
}
