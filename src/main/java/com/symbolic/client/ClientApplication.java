package com.symbolic.client;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

/**
 * Main class for the Client application that launches the Java FX UI.
 */
@SpringBootApplication
@PropertySource("classpath:application-client.properties")
public class ClientApplication {
  public static void main(String[] args) {
    Application.launch(BgcheckApplication.class, args);
  }
}
