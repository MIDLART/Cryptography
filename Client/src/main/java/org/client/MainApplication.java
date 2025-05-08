package org.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

public class MainApplication extends Application {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() {
    applicationContext = new SpringApplicationBuilder(SpringBootApp.class).run();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    URL url = getClass().getResource("fxml/auth.fxml");

    FXMLLoader fxmlLoader = new FXMLLoader(url);
    Scene scene = new Scene(fxmlLoader.load());
    primaryStage.setTitle("Авторизация");
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  @Override
  public void stop() {
    applicationContext.close();
    Platform.exit();
  }

  @SpringBootApplication
  public static class SpringBootApp {
    public static void main(String[] args) {
      Application.launch(MainApplication.class, args);
    }
  }
}