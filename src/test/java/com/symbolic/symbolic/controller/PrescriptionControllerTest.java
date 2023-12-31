package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.MedicalPractitioner;
import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.entity.Prescription;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Tag("UnitTest")
public class PrescriptionControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  PrescriptionRepository prescriptionRepository;
  @MockBean
  PatientRepository patientRepository;
  @MockBean
  MedicalPractitionerRepository practitionerRepository;
  @InjectMocks
  PrescriptionController prescriptionController;

  AutoCloseable openMocks;

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
    assertEquals(id, PrescriptionController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(PrescriptionController.parseUuidFromString("test"));
    assertNull(PrescriptionController.parseUuidFromString("2"));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetAllPrescriptions() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 3, 50, "Different instructions");
    List<Prescription> prescriptions = new ArrayList<>();
    when(prescriptionRepository.findAll()).thenReturn(prescriptions);

    // Test when no prescriptions exist
    mockMvc.perform(get("/api/prescriptions")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());


    // Test when prescriptions are returned
    prescriptions.add(prescription1);
    prescriptions.add(prescription2);

    mockMvc.perform(get("/api/prescriptions")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testGetPrescriptionById() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    UUID id = UUID.randomUUID();
    prescription1.setId(id);
    when(prescriptionRepository.findById(id)).thenReturn(Optional.of(prescription1));

    // Test retrieving a prescription with a valid id
    mockMvc.perform(get("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testCreatePrescription() throws Exception {
    // Create valid prescription
    mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"1\", " +
                "\"dailyUses\": \"2\", " +
                "\"cost\": \"100\", " +
                "\"instructions\": \"Test instructions\"}"))
        .andExpect(status().isCreated());

    // Creating prescriptions with missing fields
    MvcResult result1 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dailyUses\": \"2\", " +
                "\"cost\": \"100\", " +
                "\"instructions\": \"Test instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'dosage' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"1\", " +
                "\"cost\": \"100\", " +
                "\"instructions\": \"Test instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'dailyUses' field in request body", result2.getResponse().getContentAsString());

    MvcResult result3 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"1\", " +
                "\"dailyUses\": \"2\", " +
                "\"instructions\": \"Test instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'cost' field in request body", result3.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result5 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"2\", " +
                "\"dailyUses\": \"3\", " +
                "\"cost\": \"-1\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'cost' field must be a non-negative integer", result5.getResponse().getContentAsString());

    MvcResult result6 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"-1\", " +
                "\"dailyUses\": \"3\", " +
                "\"cost\": \"10\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dosage' field must be a non-negative integer", result6.getResponse().getContentAsString());

    MvcResult result7 = mockMvc.perform(post("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"dosage\": \"2\", " +
                "\"dailyUses\": \"-1\", " +
                "\"cost\": \"10\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dailyUses' field must be a non-negative integer", result7.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testUpdatePrescription() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    UUID id = UUID.randomUUID();
    prescription1.setId(id);
    when(prescriptionRepository.findById(id)).thenReturn(Optional.of(prescription1));

    // Test updating a prescription with a valid id
    mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dosage\": \"2\", " +
                "\"dailyUses\": \"3\", " +
                "\"cost\": \"50\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + id2, result3.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result5 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dosage\": \"2\", " +
                "\"dailyUses\": \"3\", " +
                "\"cost\": \"-1\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'cost' field must be a non-negative integer", result5.getResponse().getContentAsString());

    MvcResult result6 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dosage\": \"-1\", " +
                "\"dailyUses\": \"3\", " +
                "\"cost\": \"10\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dosage' field must be a non-negative integer", result6.getResponse().getContentAsString());

    MvcResult result7 = mockMvc.perform(put("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"dosage\": \"2\", " +
                "\"dailyUses\": \"-1\", " +
                "\"cost\": \"10\", " +
                "\"instructions\": \"New instructions\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'dailyUses' field must be a non-negative integer", result7.getResponse().getContentAsString());
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeletePrescription() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    UUID id = UUID.randomUUID();
    prescription1.setId(id);
    when(prescriptionRepository.findById(id)).thenReturn(Optional.of(prescription1));

    // Test deleting a prescription with a valid id
    mockMvc.perform(delete("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No prescription found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting patient and practitioner fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    prescription1.setPatient(patient);
    prescription1.setPractitioner(practitioner);
    mockMvc.perform(delete("/api/prescription")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertFalse(patient.getPrescriptions().contains(prescription1));
    assertFalse(practitioner.getPrescriptions().contains(prescription1));
  }

  @Test
  @WithMockUser(username = "user1", password = "pwd", roles = "ADMIN")
  public void testDeleteAllPrescriptions() throws Exception {
    Prescription prescription1 = new Prescription(1, 2, 100, "Test instructions");
    Prescription prescription2 = new Prescription(2, 3, 50, "Different instructions");
    List<Prescription> prescriptions = new ArrayList<>();
    prescriptions.add(prescription1);
    prescriptions.add(prescription2);
    when(prescriptionRepository.findAll()).thenReturn(prescriptions);

    // Test deleting all prescriptions
    mockMvc.perform(delete("/api/prescriptions")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting patient and practitioner fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    MedicalPractitioner practitioner = new MedicalPractitioner(40.7, 74.0, "Surgery", 50, 10);
    prescription1.setPatient(patient);
    prescription1.setPractitioner(practitioner);

    mockMvc.perform(delete("/api/prescriptions")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    assertFalse(patient.getPrescriptions().contains(prescription1));
    assertFalse(practitioner.getPrescriptions().contains(prescription1));
  }
}
