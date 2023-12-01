package com.symbolic.client;

import com.symbolic.client.BgcheckApplication.StageReadyEvent;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * Helper class for initializing the Java FX stage.
 */
@Component
public class StageInitializer implements ApplicationListener<StageReadyEvent> {
  @Value("classpath:/client.fxml")
  private Resource chartResource;
  private String applicationTitle;

  public StageInitializer(@Value("${spring.application.ui.title}") String applicationTitle) {
    this.applicationTitle = applicationTitle;
  }

  /**
   * Main handler method that defines how to load the custom client defined in FXML.
   *
   * @param event the event raised when the stage is ready to load
   */
  public void onApplicationEvent(StageReadyEvent event) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(chartResource.getURL());
      Parent parent = fxmlLoader.load();
      Stage stage = event.getStage();
      stage.setScene(new Scene(parent, 600, 400));
      stage.setTitle(applicationTitle);
      stage.show();
    } catch (IOException e) {
      throw new RuntimeException();
    }
  }
}