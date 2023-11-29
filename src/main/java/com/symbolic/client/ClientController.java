package com.symbolic.client;

import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
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

        try {
            // Get information to populate label fields
            boolean[] results = submitRequest(subjectId.getText(), vaccinationBox.getValue(), allergyBox.getValue(), diagnosisBox.getValue());
        } catch (URISyntaxException e) {
            System.err.println("Bad URI trying to fetch data.");
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

    private boolean[] submitRequest(String subjectId, String vaccination, String allergy, String diagnosis,
                                    @Value("${spring.application.default-uri}") String uri) throws URISyntaxException {

        HttpRequest request = HttpRequest.newBuilder(new URI(uri))
                .POST()
                .timeout(Duration.of(3, SECONDS))
                .build();
    }
}