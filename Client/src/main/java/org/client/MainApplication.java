package org.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class MainApplication extends Application {
  private ConfigurableApplicationContext applicationContext;

  @Override
  public void init() {
    applicationContext = new SpringApplicationBuilder(SpringBootApp.class).run();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
//    FXMLLoader loader = applicationContext.getBean(FXMLLoader.class);
//    loader.setLocation(getClass().getResource("/fxml/main.fxml"));
//
//    Parent root = loader.load();
//
//    primaryStage.setTitle("Crypto Application");
//    primaryStage.setScene(new Scene(root, 800, 600));
//    primaryStage.show();
    FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("fxml/main.fxml"));
    Scene scene = new Scene(fxmlLoader.load(), 320, 240);
    primaryStage.setTitle("Hello!");
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