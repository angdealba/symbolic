package com.symbolic.client;

import com.fasterxml.jackson.databind.util.JSONPObject;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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

    @FXML
    protected void submitButtonPressed() {

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

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse response
        HttpHeaders responseHeaders = response.headers();
        String responseBody = response.body();

        boolean[] result = new boolean[3];

        String[] responseSplit = responseBody.split(",");
        result[0] = responseSplit[1].toLowerCase().contains("true");
        result[1] = responseSplit[2].toLowerCase().contains("true");
        result[2] = responseSplit[3].toLowerCase().contains("true");

        return result;
    }
}