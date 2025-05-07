package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Log4j2
@Controller
@RequiredArgsConstructor
public class AuthController {
  @FXML
  private TextField usernameField;
  @FXML
  private PasswordField passwordField;

  @FXML
  private Button registerButton;
  @FXML
  private Label messageLabel;

  private String authToken;

  @FXML
  private void registration() throws JsonProcessingException {
    clearLabel();

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
        showSuccess("Регистрация успешна!");
      } else {
        showError("Ошибка: " + response.body());
      }
    } catch (IOException | InterruptedException e) {
      log.error("Registration error", e);
      showError("Ошибка соединения с сервером: " + e.getMessage());
    }
  }

  @FXML
  private void login() throws JsonProcessingException {
    clearLabel();

    String username = usernameField.getText();
    String password = passwordField.getText();

    if (username.isEmpty() || password.isEmpty()) {
      showError("Все поля обязательны для заполнения!");
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
        showError("Ошибка парсинга: " + e.getMessage());

        return;
      }

      if (response.statusCode() == 200) {
        authToken = loginResponse.getToken();
        showSuccess(loginResponse.getMessage());

        loadMainView();
      } else {
        showError(loginResponse.getMessage());
      }
    } catch (IOException | InterruptedException e) {
      log.error("Login error", e);
      showError("Ошибка соединения с сервером: " + e.getMessage());
    }
  }

  private void loadMainView() {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/client/fxml/main.fxml"));
      Parent root = loader.load();

      Scene currentScene = usernameField.getScene();
      Stage stage = (Stage) currentScene.getWindow();

      Scene mainScene = new Scene(root);
      stage.setScene(mainScene);
      stage.setTitle("Главный экран");
      stage.show();

      UserController mainController = loader.getController();
      mainController.setAuthToken(authToken);

    } catch (IOException e) {
      log.error("Failed to load main view", e);
      showError("Ошибка загрузки главного экрана");
    }
  }

  private void testAuthenticatedRequest() {
    try {
      HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:8080/meow"))
              .header("Authorization", "Bearer " + authToken)
              .GET()
              .build();

      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        showSuccess("Авторизованный запрос успешен: " + response.body());
      } else {
        showError("Ошибка авторизованного запроса: " + response.body());
      }
    } catch (IOException | InterruptedException e) {
      log.error("Authenticated request error", e);
      showError("Ошибка выполнения запроса: " + e.getMessage());
    }
  }

  private void showError(String message) {
    messageLabel.setText(message);
    messageLabel.setStyle("-fx-text-fill: red;");
  }

  private void showSuccess(String message) {
    messageLabel.setText(message);
    messageLabel.setStyle("-fx-text-fill: green;");
  }

  private void clearLabel() {
    messageLabel.setText("");
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class RegistrationRequest {
    private String name;
    private String password;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public class RegistrationResponse {
    private String message;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class LoginRequest {
    private String username;
    private String password;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class LoginResponse {
    private String message;
    private String token;
  }
}
