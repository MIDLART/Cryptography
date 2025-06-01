package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.client.dto.LoginRequest;
import org.client.dto.LoginResponse;
import org.client.dto.RegistrationRequest;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.UnaryOperator;

import static org.client.services.CommonService.*;

@Log4j2
@Controller
@RequiredArgsConstructor
public class AuthController {
  @FXML private TextField usernameField;
  @FXML private PasswordField passwordField;

  @FXML private Button registerButton;
  @FXML public Button loginButton;
  @FXML private Label messageLabel;

  private String authToken;

  public void initialize() {
    UnaryOperator<TextFormatter.Change> lengthFilter = change ->
            change.getControlNewText().length() > 50 ? null : change;

    usernameField.setTextFormatter(new TextFormatter<>(lengthFilter));
    passwordField.setTextFormatter(new TextFormatter<>(lengthFilter));
  }

  @FXML
  private void registration() throws JsonProcessingException {
    clearLabel(messageLabel);

    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
      messageLabel.setText("Все поля обязательны для заполнения!");
      return;
    }

    RegistrationRequest regRequest = new RegistrationRequest(username, password);
    String jsonRequest = new ObjectMapper().writeValueAsString(regRequest);

    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:8080/registration"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
              .build();

      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        showSuccess(messageLabel, "Регистрация успешна!");
      } else {
        try {
          JsonNode jsonNode = new ObjectMapper().readTree(response.body());
          String errorMessage = jsonNode.get("message").asText();
          showError(messageLabel, "Ошибка: " + errorMessage);
        } catch (Exception e) {
          showError(messageLabel, "Ошибка: " + response.body());
        }
      }
    } catch (IOException | InterruptedException e) {
      log.error("Registration error", e);
      showError(messageLabel, "Ошибка соединения с сервером: " + e.getMessage());
    }
  }

  @FXML
  private void login() throws JsonProcessingException {
    clearLabel(messageLabel);

    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
      showError(messageLabel, "Все поля обязательны для заполнения!");
      return;
    }

    LoginRequest loginRequest = new LoginRequest(username, password);
    String jsonRequest = new ObjectMapper().writeValueAsString(loginRequest);

    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:8080/login"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
              .build();

      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      LoginResponse loginResponse = null;
      try {
        loginResponse = new ObjectMapper().readValue(response.body(), LoginResponse.class);
      } catch (JsonProcessingException e) {
        log.error("Parse error {}", response.body(), e);
        showError(messageLabel, "Ошибка парсинга: " + e.getMessage());

        return;
      }

      if (response.statusCode() == 200) {
        authToken = loginResponse.getToken();
        showSuccess(messageLabel, loginResponse.getMessage());

        loadMainView();
      } else {
        showError(messageLabel, loginResponse.getMessage());
      }
    } catch (IOException | InterruptedException e) {
      log.error("Login error", e);
      showError(messageLabel, "Ошибка соединения с сервером: " + e.getMessage());
    }
  }

  private void loadMainView() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/client/fxml/main.fxml"));
      Parent root = loader.load();

      Scene currentScene = usernameField.getScene();
      Stage stage = (Stage) currentScene.getWindow();

      UserController mainController = loader.getController();
      stage.setOnCloseRequest(e -> {
        mainController.shutdown();
        Platform.exit();
        System.exit(0);
      });

      Scene mainScene = new Scene(root);
      stage.setScene(mainScene);
      stage.setTitle("Главный экран");
      stage.show();

      mainController.setAuthToken(authToken);
      mainController.setUsername(usernameField.getText());
      mainController.chats();

    } catch (IOException e) {
      log.error("Failed to load main view", e);
      showError(messageLabel, "Ошибка загрузки главного экрана");
    }
  }
}
