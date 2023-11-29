package com.symbolic.client;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) {
        Application.launch(BgcheckApplication.class, args);
    }
}
