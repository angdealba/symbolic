package com.symbolic.client;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application-client.properties")
public class ClientApplication {
    public static void main(String[] args) {
        Application.launch(BgcheckApplication.class, args);
    }
}
