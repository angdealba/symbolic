package com.symbolic.client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class ClientControllerTest {

    @Mock
    private CloseableHttpClient httpClient;
    @Mock
    private CloseableHttpResponse httpResponse;
    @Mock
    private HttpUriRequest httpRequest;
    @Mock
    private StatusLine statusLine;
    @InjectMocks
    private ClientController clientController;
    AutoCloseable openMocks;

    @BeforeEach
    void setUp() {
        clientController = new ClientController();
        openMocks = MockitoAnnotations.openMocks(this);
        httpClient = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class);
        httpRequest = mock(HttpUriRequest.class);
        statusLine = mock(StatusLine.class);
    }

    @Test
    void handleRegistrationTest() throws IOException {
        // Mocking the HTTP client behavior
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        // Simulate handleRegistration
        clientController.handleRegistration();
        // Verify that properties are updated after successful registration
        assertEquals("true", clientController.clientProps.getProperty("hasRegistered"));
    }

    @Test
    void handleAuthenticationTest() throws IOException {
        // Mocking the HTTP client behavior
        when(httpClient.execute(httpRequest)).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);

        // Simulate handleAuthentication
        clientController.handleAuthentication();
        // Verify that token is returned after authenticating
        assertNotNull(clientController.token);
    }
    @Test
    void submitButtonPressedTest() throws URISyntaxException, IOException, InterruptedException {
        clientController.token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwMTM4OTgwNCwiZXhwIjoxNzAxNjA1ODA0fQ.giOL6EXw2T0Huq0la-6iVbwrME7KHYghHZj60Ne-OnM";

        ClientController.BackgroundCheckResponse response = new ClientController.BackgroundCheckResponse();
        response.setVaccination(true);
        response.setAllergy(false);
        response.setDiagnosis(true);

        ClientController controllerSpy = Mockito.spy(clientController);
        doReturn(response).when(controllerSpy).submitRequest("", "", "", "");

        String[] output = controllerSpy.submitButtonPressed();
        assertEquals("[ POSITIVE ]", output[0]);
        assertEquals("[ NEGATIVE ]", output[1]);
        assertEquals("[ POSITIVE ]", output[2]);
    }


}


