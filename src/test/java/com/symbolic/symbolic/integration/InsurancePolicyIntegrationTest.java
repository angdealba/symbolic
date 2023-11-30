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
import org.springframework.test.context.jdbc.Sql;
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
 * Provides internal integration testing between the InsurancePolicyController and 2 Repositories
 * and Entity types, along with the authentication code and the join between Patient-Policy data.
 * Provides external integration testing between the Repositories and the database implementation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("IntegrationTest")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InsurancePolicyIntegrationTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private WebApplicationContext context;
  @Autowired
  private JdbcTemplate jdbcTemplate;
  @Autowired
  InsurancePolicyRepository insurancePolicyRepository;
  @Autowired
  PatientRepository patientRepository;

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
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "policy_patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "patients");
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "insurance_policies");
  }

  @Test
  public void testGetAllPolicies() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    InsurancePolicy policy2 = new InsurancePolicy(50);

    // Test when no policies exist
    JdbcTestUtils.deleteFromTables(jdbcTemplate, "insurance_policies");
    mockMvc.perform(get("/api/policies")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test when policies are returned
    insurancePolicyRepository.save(policy1);
    insurancePolicyRepository.save(policy2);

    mockMvc.perform(get("/api/policies")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetPolicyById() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = insurancePolicyRepository.save(policy1).getId();

    // Test retrieving a policy with a valid id
    mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test retrieving with no ID
    MvcResult result1 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test retrieving with an invalid ID
    MvcResult result2 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(get("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"premiumCost\": \"100\"}"))
        .andExpect(status().isCreated());

    // Creating policy with missing field
    MvcResult result1 = mockMvc.perform(post("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'premiumCost' field in request body", result1.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result2 = mockMvc.perform(post("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"premiumCost\": \"-1\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'premiumCost' field must be a non-negative integer", result2.getResponse().getContentAsString());
  }

  @Test
  public void testUpdatePolicy() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = insurancePolicyRepository.save(policy1).getId();

    // Test updating a policy with a valid id
    mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"premiumCost\": \"50\"}"))
        .andExpect(status().isOk());

    // Test updating with no fields does not raise error
    mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isOk());

    // Test updating with no ID
    MvcResult result1 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test updating with an invalid ID
    MvcResult result2 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test retrieving with a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + id2, result3.getResponse().getContentAsString());

    // Test negative inputs
    MvcResult result4 = mockMvc.perform(put("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\", " +
                "\"premiumCost\": \"-1\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'premiumCost' field must be a non-negative integer", result4.getResponse().getContentAsString());
  }

  @Test
  public void testDeletePolicy() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID id = insurancePolicyRepository.save(policy1).getId();

    // Test deleting a policy with a valid id
    mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id + "\"}"))
        .andExpect(status().isNoContent());

    // Test deleting with no ID
    MvcResult result1 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'id' field in request body", result1.getResponse().getContentAsString());

    // Test deleting with an invalid ID
    MvcResult result2 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\":  \"test\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'id' field must contain a valid UUID value", result2.getResponse().getContentAsString());

    // Test deleting a UUID that is not in the database
    UUID id2 = UUID.randomUUID();
    MvcResult result3 = mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + id2, result3.getResponse().getContentAsString());

    // Test deleting patient field
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy3 = new InsurancePolicy(50);
    UUID patientId = patientRepository.save(patient).getId();
    UUID policyId = insurancePolicyRepository.save(policy3).getId();

    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"id\": \"" + policyId + "\"}"))
        .andExpect(status().isNoContent());
    assertNull(patient.getInsurancePolicy());
  }

  @Test
  public void testDeleteAllPolicies() throws Exception {
    InsurancePolicy policy1 = new InsurancePolicy(100);
    InsurancePolicy policy2 = new InsurancePolicy(50);
    insurancePolicyRepository.save(policy1);
    insurancePolicyRepository.save(policy2);

    // Test deleting all diagnoses
    mockMvc.perform(delete("/api/policies")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());

    // Test deleting patient fields
    Patient patient = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy3 = new InsurancePolicy(50);
    UUID patientId = patientRepository.save(patient).getId();
    UUID policyId = insurancePolicyRepository.save(policy3).getId();

    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(delete("/api/policies")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString))
        .andExpect(status().isNoContent());
    assertNull(patient.getInsurancePolicy());
  }

  @Test
  public void testGetPatientsByPolicyId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    Patient patient2 = new Patient("Flu", "Tree Nut", "None");
    InsurancePolicy policy1 = new InsurancePolicy(100);
    policy1.addPatient(patient1);
    policy1.addPatient(patient2);
    UUID policyId = insurancePolicyRepository.save(policy1).getId();

    // Test retrieving patients
    MvcResult result = mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk()).andReturn();

    // Test missing ID
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"policyId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/policy/patients")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"policyId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetPolicyByPatientId() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID patientId = patientRepository.save(patient1).getId();

    // Test retrieving policy
    MvcResult result = mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isOk())
        .andReturn();

    // Test missing ID
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{}"))
        .andExpect(status().isBadRequest());

    // Test invalid ID
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\":  \"test\"}"))
        .andExpect(status().isBadRequest());

    // Test an ID that is not in the database
    UUID id2 = UUID.randomUUID();
    mockMvc.perform(get("/api/patient/policy")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + id2 + "\"}"))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testAddJoinPatientPolicy() throws Exception {
    Patient patient1 = new Patient("COVID-19", "Dairy", "Wheelchair Access");
    InsurancePolicy policy1 = new InsurancePolicy(100);
    UUID patientId = patientRepository.save(patient1).getId();
    UUID policyId = insurancePolicyRepository.save(policy1).getId();

    // Test joining patient-policy
    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    // Test missing patient or policy ID
    MvcResult result1 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'policyId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'policyId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID policyId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + policyId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
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
    UUID patientId = patientRepository.save(patient1).getId();
    UUID policyId = insurancePolicyRepository.save(policy1).getId();

    // Test joining patient-policy
    mockMvc.perform(post("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isOk());

    // Test removing patient-policy
    mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isNoContent());

    // Test missing patient or policy ID
    MvcResult result1 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'policyId' field in request body", result1.getResponse().getContentAsString());

    MvcResult result2 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("Missing 'patientId' field in request body", result2.getResponse().getContentAsString());

    // Test invalid IDs
    MvcResult result3 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"2\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'patientId' field must contain a valid UUID value", result3.getResponse().getContentAsString());

    MvcResult result4 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"2\"}"))
        .andExpect(status().isBadRequest())
        .andReturn();
    assertEquals("'policyId' field must contain a valid UUID value", result4.getResponse().getContentAsString());

    // Test IDs not in the database
    UUID policyId2 = UUID.randomUUID();
    MvcResult result5 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId + "\", " +
                "\"policyId\": \"" + policyId2 + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No insurance policy found with id " + policyId2, result5.getResponse().getContentAsString());

    UUID patientId2 = UUID.randomUUID();
    MvcResult result6 = mockMvc.perform(delete("/api/policy/patient")
            .contentType(MediaType.APPLICATION_JSON)
            .header(HttpHeaders.AUTHORIZATION, tokenString)
            .content("{\"patientId\": \"" + patientId2 + "\", " +
                "\"policyId\": \"" + policyId + "\"}"))
        .andExpect(status().isNotFound())
        .andReturn();
    assertEquals("No patient found with id " + patientId2, result6.getResponse().getContentAsString());
  }
}
