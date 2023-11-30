package com.symbolic.client;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.methods.HttpUriRequest;
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

        ClientController.BackgroundCheckResponse response = new ClientController.BackgroundCheckResponse();
        response.setVaccination(true);
        response.setAllergy(false);
        response.setDiagnosis(true);
        when(clientController.submitRequest(any(), any(), any(), any())).thenReturn(response);

        clientController.submitButtonPressed();
        assertEquals("[ POSITIVE ]", clientController.vaccinationResultLabel.getText());
        assertEquals("[ NEGATIVE ]", clientController.allergyResultLabel.getText());
        assertEquals("[ POSITIVE ]", clientController.diagnosisResultLabel.getText());
    }


}


