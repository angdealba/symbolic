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
public class InsurancePolicyControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @MockBean
  InsurancePolicyRepository insurancePolicyRepository;
  @MockBean
  PatientRepository patientRepository;
  @InjectMocks
  InsurancePolicyController policyController;

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
    assertEquals(id, InsurancePolicyController.parseUuidFromString(idString));

    // Test invalid IDs
    assertNull(InsurancePolicyController.parseUuidFromString("test"));
    assertNull(InsurancePolicyController.parseUuidFromString("2"));
  }

  @Test
  public void testGetAllPolicies() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    InsurancePolicy policy2 = new InsurancePolicy(50);
    List<InsurancePolicy> policies = new ArrayList<>();
    when(insurancePolicyRepository.findAll()).thenReturn(policies);

    // Test when no appointments exist
    mockMvc.perform(get("/api/policies")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());


    // Test when prescriptions are returned
    policies.add(policy1);
    policies.add(policy2);

    mockMvc.perform(get("/api/policies")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetPolicyById() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = UUID.randomUUID();
    policy1.setId(id);
    when(insurancePolicyRepository.findById(id)).thenReturn(Optional.of(policy1));

    // Test retrieving a policy with a valid id
    mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testCreatePolicy() throws Exception {
    // Create valid policy
    mockMvc.perform(post("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"premiumCost\": \"100\"}"))
        .andExpect(status().isCreated());

    // Creating policy with missing field
    MvcResult result1 = mockMvc.perform(post("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'premiumCost' field in request body", result1.getResponse().getContentAsString());
  }

  @Test
  public void testUpdatePolicy() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = UUID.randomUUID();
    policy1.setId(id);
    when(insurancePolicyRepository.findById(id)).thenReturn(Optional.of(policy1));

    // Test updating a policy with a valid id
    mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\", " +
                "\"premiumCost\": \"50\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + id2, result3.getResponse().getContentAsString());
  }

  @Test
  public void testDeletePolicy() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = UUID.randomUUID();
    policy1.setId(id);
    when(insurancePolicyRepository.findById(id)).thenReturn(Optional.of(policy1));

    // Test deleting a policy with a valid id
    mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting patient field
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    policy1.addPatient(patient);
    mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());
    assertNull(patient.getInsurancePolicy());
  }

  @Test
  public void testDeleteAllPolicies() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    InsurancePolicy policy2 = new InsurancePolicy(50);
    List<InsurancePolicy> policies = new ArrayList<>();
    policies.add(policy1);
    policies.add(policy2);
    when(insurancePolicyRepository.findAll()).thenReturn(policies);

    // Test deleting all diagnoses
    mockMvc.perform(delete("/api/policies")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());

    // Test deleting patient fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    policy1.addPatient(patient);

    mockMvc.perform(delete("/api/policies")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
    assertNull(patient.getInsurancePolicy());
  }

  @Test
  public void testGetPatientsByPolicyId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    List<Patient> patients = new ArrayList<>();
    patients.add(patient1);
    patients.add(patient2);
    UUID id = UUID.randomUUID();
    when(insurancePolicyRepository.existsById(id)).thenReturn(true);
    when(patientRepository.findPatientsByInsurancePolicyId(id)).thenReturn(patients);

    // Test retrieving patients
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"policyId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"policyId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"policyId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPolicyByPatientId() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = UUID.randomUUID();
    when(patientRepository.existsById(id)).thenReturn(true);
    when(insurancePolicyRepository.findInsurancePolicyByPatientsId(id)).thenReturn(policy1);

    // Test retrieving policy
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test missing ID
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientPolicy() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID patientId = UUID.randomUUID();
    UUID policyId = UUID.randomUUID();

    when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient1));
    when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(policy1));

    // Test joining patient-policy
    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    // Test with valid old policy
    when(insurancePolicyRepository.findInsurancePolicyByPatientsId(patientId)).thenReturn(policy1);
    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or policy ID
    MvcResult result1 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'policyId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'policyId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID policyId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + policyId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }

  @Test
  public void testRemoveJoinPatientPolicy() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID patientId = UUID.randomUUID();
    UUID policyId = UUID.randomUUID();

    when(patientRepository.existsById(patientId)).thenReturn(true);
    when(insurancePolicyRepository.findById(policyId)).thenReturn(Optional.of(policy1));

    // Test removing patient-policy
    mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing patient or policy ID
    MvcResult result1 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'policyId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"2\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'policyId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID policyId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + policyId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }
}
