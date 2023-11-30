package com.symbolic.client;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Properties;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.SECONDS;

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
  private ChoiceBox<String> vaccinationBox;
  @FXML
  private ChoiceBox<String> allergyBox;
  @FXML
  private ChoiceBox<String> diagnosisBox;

  /**
   * Custom User authentication request consisting of a username and password.
   */
  public static class AuthenticationRequest {
    private String name;
    private String password;

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

    public String getToken() {
      return token;
    }
  }


  private String token = null;

  private Properties clientProps;
  private String configPath;

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
      System.out.println("Reached a critical error initializing the ClientController");
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
      System.out.println("There was an error registering the user.");
      throw new RuntimeException(e);
    }
  }

  /**
   * Handles client registration if it has not been registered yet.
   */
  private void handleRegistration() throws IOException {
    // Check if the client has been registered
    if (clientProps.getProperty("hasRegistered") == null) {
      String uri = "http://localhost:8080/api/client/register";

      String name = clientProps.getProperty("clientName");
      String password = clientProps.getProperty("clientPassword");

      AuthenticationRequest authRequest = new AuthenticationRequest(name, password);
      Gson gson = new Gson();
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpPost postRequest = new HttpPost(uri);
      StringEntity postBody = new StringEntity(gson.toJson(authRequest));
      postRequest.setEntity(postBody);
      postRequest.setHeader("Content-Type", "application/json");
      HttpResponse response = client.execute(postRequest);

      if (response.getStatusLine().getStatusCode() == 200) {
        System.out.println("Successfully registered client " + name);
        clientProps.setProperty("hasRegistered", "true");
        clientProps.store(new FileOutputStream(configPath), null);
      } else {
        throw new IOException("There was an error registering the client " + name);
      }
    }
  }

  private void handleAuthentication() throws IOException {
    // Check if the token has been loaded from the service
    if (token == null) {
      String uri = "http://localhost:8080/api/client/authenticate";

      String name = clientProps.getProperty("clientName");
      String password = clientProps.getProperty("clientPassword");

      AuthenticationRequest authRequest = new AuthenticationRequest(name, password);
      Gson gson = new Gson();
      CloseableHttpClient client = HttpClientBuilder.create().build();
      HttpPost postRequest = new HttpPost(uri);
      StringEntity postBody = new StringEntity(gson.toJson(authRequest));
      postRequest.setEntity(postBody);
      postRequest.setHeader("Content-Type", "application/json");
      HttpResponse response = client.execute(postRequest);
      System.out.println(response.getStatusLine());

      if (response.getStatusLine().getStatusCode() == 200) {
        System.out.println("Successfully authenticated client " + name);
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        AuthenticationResponse authResponse = gson.fromJson(responseString, AuthenticationResponse.class);
        token = authResponse.getToken();
        System.out.println(token);
      } else {
        throw new IOException("There was an error authenticating the client " + name);
      }
    }
  }

  @FXML
  protected void submitButtonPressed() {
    // Check if the authentication token is null
    if (token == null) {
      try {
        handleAuthentication();
      } catch (IOException e) {
        System.out.println("The client was unable to authenticate with the background check service.");
        return;
      }
    }

    boolean[] results;

    try {
      // Get information to populate label fields
      results = submitRequest(subjectId.getText(), vaccinationBox.getValue(), allergyBox.getValue(),
          diagnosisBox.getValue());
    } catch (URISyntaxException e) {
      System.err.println("Bad URI trying to fetch data.");
      return;
    } catch (IOException e) {
      System.err.println("Error with HTTP client.");
      return;
    } catch (InterruptedException e) {
      System.err.println("HTTP client interrupted.");
      return;
    }

    // Update fields
    if (results[0]) {
      vaccinationResultLabel.setText("[ PASS! ]");
    } else {
      vaccinationResultLabel.setText("[ FAIL! ]");
    }

    if (!results[1]) {
      allergyResultLabel.setText("[ PASS! ]");
    } else {
      allergyResultLabel.setText("[ FAIL! ]");
    }

    if (!results[2]) {
      diagnosisResultLabel.setText("[ PASS! ]");
    } else {
      diagnosisResultLabel.setText("[ FAIL! ]");
    }
  }

  private final HttpClient httpClient = HttpClient.newBuilder().build();

  private boolean[] submitRequest(String subjectId, String vaccination, String allergy, String diagnosis)
      throws URISyntaxException, IOException, InterruptedException {

    // URI/URL for the service bgcheck API endpoint
    String uri = "http://localhost:8080/api/bgcheck";   // Is there a better way than hardcode?

    // Hopefully not malformed JSON object string
    String requestBody = String.format("{\"id\": \"%s,\" \"vaccination\": \"%s\", \"allergy\": \"%s\", \"diagnosis\": \"%s\"}",
        subjectId, vaccination, allergy, diagnosis);

    HttpRequest request = HttpRequest.newBuilder(new URI(uri))
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .timeout(Duration.of(2, SECONDS))
        .build();

//    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//
//    // Parse response
//    HttpHeaders responseHeaders = response.headers();
//    String responseBody = response.body();
//    vaccinationResultLabel.setText(response.toString());
//
    boolean[] result = new boolean[3];
//
//    String[] responseSplit = responseBody.split(",");
//    result[0] = responseSplit[1].toLowerCase().contains("true");
//    result[1] = responseSplit[2].toLowerCase().contains("true");
//    result[2] = responseSplit[3].toLowerCase().contains("true");

//    return result;
    return result;
  }
}