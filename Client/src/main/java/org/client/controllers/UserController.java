package org.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UserController {
  @Setter
  @Getter
  private String authToken;

  @FXML
  private Button button;

  @FXML
  private Hyperlink registrationLink;

  @FXML
  private void meow() throws IOException, InterruptedException {
    log.info("meow");
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/meow"))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    button.setText(response.body());
  }

  @FXML
  private void goToRegistration() {
    try {
      URL url = getClass().getResource("/org/client/fxml/auth.fxml");
      if (url == null) {
        throw new IOException("FXML file not found");
      }

      FXMLLoader loader = new FXMLLoader(url);
      Parent root = loader.load();

      Stage stage = (Stage) registrationLink.getScene().getWindow();

      stage.setScene(new Scene(root));
      stage.setTitle("Регистрация");
    } catch (IOException e) {
      log.error("Ошибка при загрузке формы регистрации", e);
    }
  }
}
