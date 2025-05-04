package org.client.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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
public class UserController {

//  @FXML
//  private TextField usernameField;
//  @FXML
//  private PasswordField passwordField;
//  @FXML
//  private PasswordField confirmPasswordField;

  @FXML
  private Button button;

  @FXML
  private void meow() throws IOException, InterruptedException {
    log.info("meow");
    HttpClient client = HttpClient.newHttpClient();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/meow"))
            .GET()
            .build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    button.setText(response.body());
  }

//  private void clearFields() {
//    usernameField.clear();
//    passwordField.clear();
//    confirmPasswordField.clear();
//  }
//
//  private void showAlert(Alert.AlertType type, String title, String message) {
//    Alert alert = new Alert(type);
//    alert.setTitle(title);
//    alert.setHeaderText(null);
//    alert.setContentText(message);
//    alert.showAndWait();
//  }
}
