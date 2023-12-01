package com.symbolic.client;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.util.Properties;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

/**
 * Controller class that defines all of the functionality of the client application's Java FX UI.
 */
@Component
public class ClientController {
  @FXML
  private Label vaccinationResultLabel;
  @FXML
  private Label allergyResultLabel;
  @FXML
  private Label diagnosisResultLabel;

  @FXML
  private TextField subjectId;

  @FXML
  private ComboBox<String> vaccinationBox;
  @FXML
  private ComboBox<String> allergyBox;
  @FXML
  private ComboBox<String> diagnosisBox;

  /**
   * Custom User authentication request consisting of a username and password.
   */
  public static class AuthenticationRequest {
    private String name;
    private String password;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public AuthenticationRequest(String name, String password) {
      this.name = name;
      this.password = password;
    }
  }

  /**
   * Custom response from User authentication requests consisting of a JSON web token string.
   */
  public static class AuthenticationResponse {
    private String token;

    public void setToken(String token) {
      this.token = token;
    }

    public String getToken() {
      return token;
    }
  }

  /**
   * Custom request object used to represent requests to the /bgcheck service.
   */
  public static class BackgroundCheckBody {
    private String id;
    private String vaccination;
    private String allergy;
    private String diagnosis;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getVaccination() {
      return vaccination;
    }

    public void setVaccination(String vaccination) {
      this.vaccination = vaccination;
    }

    public String getAllergy() {
      return allergy;
    }

    public void setAllergy(String allergy) {
      this.allergy = allergy;
    }

    public String getDiagnosis() {
      return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
      this.diagnosis = diagnosis;
    }

    public BackgroundCheckBody(String id, String vaccination, String allergy, String diagnosis) {
      this.id = id;
      this.vaccination = vaccination;
      this.allergy = allergy;
      this.diagnosis = diagnosis;
    }
  }

  /**
   * Custom request object used to represent responses from the /bgcheck service.
   */
  public static class BackgroundCheckResponse {
    private boolean vaccination;
    private boolean allergy;
    private boolean diagnosis;

    public boolean isVaccination() {
      return vaccination;
    }

    public void setVaccination(boolean vaccination) {
      this.vaccination = vaccination;
    }

    public boolean isAllergy() {
      return allergy;
    }

    public void setAllergy(boolean allergy) {
      this.allergy = allergy;
    }

    public boolean isDiagnosis() {
      return diagnosis;
    }

    public void setDiagnosis(boolean diagnosis) {
      this.diagnosis = diagnosis;
    }

    public boolean matchesVaccination() {
      return vaccination;
    }

    public boolean matchesAllergy() {
      return allergy;
    }

    public boolean matchesDiagnosis() {
      return diagnosis;
    }
  }


  public String token = null;

  public Properties clientProps;
  public String configPath;

  /**
   * Initializes the property reader for the client authentication details.
   */
  public ClientController() {
    String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
    configPath = rootPath + "client.properties";

    clientProps = new Properties();
    try {
      // Create the properties file if it does not already exist
      File f = new File(configPath);

      if (!f.exists()) {
        // Create the file and load the properties object
        f.createNewFile();
        clientProps.load(new FileInputStream(configPath));

        // Use the UUID generator to generate randomized strings for authentication details
        String newName = UUID.randomUUID().toString();
        String newPassword = UUID.randomUUID().toString();

        // Set the property values and write to the file
        clientProps.setProperty("clientName", newName);
        clientProps.setProperty("clientPassword", newPassword);
        clientProps.store(new FileOutputStream(configPath), null);
      } else {
        clientProps.load(new FileInputStream(configPath));
      }
    } catch (IOException e) {
      System.err.println("Reached a critical error initializing the ClientController");
      throw new RuntimeException(e);
    }
  }

  /**
   * Perform the registration with the service when the UI initializes.
   */
  @FXML
  public void initialize() {
    try {
      handleRegistration();
    } catch (Exception e) {
      System.err.println("There was an error registering the user.");
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles client registration if it has not been registered yet.
   */
  public void handleRegistration() throws IOException {
    // Check if the client has been registered
    if (clientProps.getProperty("hasRegistered") == null) {
      String uri = "http://localhost:8080/api/client/register";

      String name = clientProps.getProperty("clientName");
      String password = clientProps.getProperty("clientPassword");

      // Perform the POST request to /api/client/register
      AuthenticationRequest authRequest = new AuthenticationRequest(name, password);
      Gson gson = new Gson();
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpPost postRequest = new HttpPost(uri);
      StringEntity postBody = new StringEntity(gson.toJson(authRequest));
      postRequest.setEntity(postBody);
      postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      HttpResponse response = client.execute(postRequest);

      if (response.getStatusLine().getStatusCode() == 200) {
        clientProps.setProperty("hasRegistered", "true");
        clientProps.store(new FileOutputStream(configPath), null);
      } else {
        throw new IOException("There was an error registering the client " + name);
      }
    }
  }

  public void handleAuthentication() throws IOException {
    // Check if the token has been loaded from the service
    if (token == null) {
      String uri = "http://localhost:8080/api/client/authenticate";

      String name = clientProps.getProperty("clientName");
      String password = clientProps.getProperty("clientPassword");

      // Perform the POST request to /api/client/authenticate
      AuthenticationRequest authRequest = new AuthenticationRequest(name, password);
      Gson gson = new Gson();
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpPost postRequest = new HttpPost(uri);
      StringEntity postBody = new StringEntity(gson.toJson(authRequest));
      postRequest.setEntity(postBody);
      postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
      HttpResponse response = client.execute(postRequest);

      if (response.getStatusLine().getStatusCode() == 200) {
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        AuthenticationResponse authResponse = gson.fromJson(responseString,
            AuthenticationResponse.class);
        token = authResponse.getToken();
      } else {
        throw new IOException("There was an error authenticating the client " + name);
      }
    }
  }

  @FXML
  public String[] submitButtonPressed() {
    String[] output = new String[3];
    // Check if the authentication token is null
    if (token == null) {
      try {
        handleAuthentication();
      } catch (IOException e) {
        System.err.println("The client could not authenticate with the background check service.");
        return output;
      }
    }

    BackgroundCheckResponse results;

    try {
      // Get information to populate label fields
      String patientId = subjectId == null ? "" : subjectId.getText();
      String vaccination = vaccinationBox == null ? "" : vaccinationBox.getValue();
      String allergy = allergyBox == null ? "" : allergyBox.getValue();
      String diagnosis = diagnosisBox == null ? "" : diagnosisBox.getValue();

      results = submitRequest(patientId, vaccination, allergy, diagnosis);
    } catch (URISyntaxException e) {
      System.err.println("Bad URI trying to fetch data.");
      return output;
    } catch (IOException e) {
      System.err.println("Error with HTTP client.");
      return output;
    } catch (InterruptedException e) {
      System.err.println("HTTP client interrupted.");
      return output;
    }

    // Update fields
    if (results.matchesVaccination()) {
      output[0] = "[ POSITIVE ]";
    } else {
      output[0] = "[ NEGATIVE ]";
    }

    if (results.matchesAllergy()) {
      output[1] = "[ POSITIVE ]";
    } else {
      output[1] = "[ NEGATIVE ]";
    }

    if (results.matchesDiagnosis()) {
      output[2] = "[ POSITIVE ]";
    } else {
      output[2] = "[ NEGATIVE ]";
    }

    if (vaccinationResultLabel != null) {
      vaccinationResultLabel.setText(output[0]);
    }
    if (allergyResultLabel != null) {
      allergyResultLabel.setText(output[1]);
    }
    if (diagnosisResultLabel != null) {
      diagnosisResultLabel.setText(output[2]);
    }

    return output;
  }

  public BackgroundCheckResponse submitRequest(String subjectId, String vaccination,
                                               String allergy, String diagnosis)
      throws URISyntaxException, IOException, InterruptedException {

    // URI/URL for the service bgcheck API endpoint
    String uri = "http://localhost:8080/api/bgcheck";

    // Perform the GET request to /api/bgcheck
    BackgroundCheckBody requestBody
        = new BackgroundCheckBody(subjectId, vaccination, allergy, diagnosis);
    Gson gson = new Gson();
    CloseableHttpClient client = HttpClientBuilder.create().build();
    StringEntity getBody = new StringEntity(gson.toJson(requestBody));
    HttpUriRequest getRequest = RequestBuilder.get(uri)
        .setEntity(getBody)
        .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
    HttpResponse response = client.execute(getRequest);

    if (response.getStatusLine().getStatusCode() == 200) {
      String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
      BackgroundCheckResponse bgCheckResponse = gson.fromJson(responseString,
          BackgroundCheckResponse.class);
      return bgCheckResponse;
    } else {
      throw new IOException("There was an error performing the background check.");
    }
  }
}