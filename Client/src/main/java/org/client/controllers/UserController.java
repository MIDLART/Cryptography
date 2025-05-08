package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.client.services.ChatService;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static org.client.services.CommonService.*;

@Log4j2
@Controller
public class UserController {
  private final ChatService chatService = new ChatService();

  @Setter
  @Getter
  private String authToken;
  @Setter
  @Getter
  private String username;

  @FXML private Button button;
  @FXML private Hyperlink authLink;

  @FXML private TextField messageField;

  @FXML private Label messageLabel;

  private Path chatFile;
  @FXML private TextArea chatArea;
  @FXML private Button loadChatButton;
  @FXML private HBox messageControls;

  @FXML
  private TextField recipientField;

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
  private void loadChat() {
    if (username == null || username.isEmpty()) {
      showError(messageLabel, "Имя пользователя не установлено");
      return;
    }

    try {
      chatFile = chatService.openChat(username, chatArea);
      messageControls.setVisible(true);
    } catch (IOException e) {
      showError(messageLabel, "Ошибка загрузки чата: " + e.getMessage());
    }
  }

  @FXML
  private void sendMessage() throws JsonProcessingException {
    if (!messageControls.isVisible()) {
      return;
    }

    String message = messageField.getText();
    String recipient = recipientField.getText();

    if (recipient == null || recipient.trim().isEmpty()) {
      showError(messageLabel, "Укажите получателя");
      return;
    }

    if (message == null || message.trim().isEmpty()) {
      return;
    }

    if (authToken == null || authToken.isEmpty()) {
      log.error("Токен не установлен!");
      return;
    }

    MessageRequest messageRequest = new MessageRequest(username, recipient, message);
    String jsonRequest = new ObjectMapper().writeValueAsString(messageRequest);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/send-message"))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

    try {
      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        chatService.writeMessage(chatFile, message, chatArea);
      } else {
        showError(messageLabel, "Ошибка сервера: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Message sending error", e);
      showError(messageLabel, "Ошибка отправки сообщения: " + e.getMessage());
    }

    messageField.clear();
  }

  @FXML
  private void goToAuth() {
    try {
      URL url = getClass().getResource("/org/client/fxml/auth.fxml");
      if (url == null) {
        throw new IOException("FXML file not found");
      }

      FXMLLoader loader = new FXMLLoader(url);

      Stage stage = (Stage) authLink.getScene().getWindow();

      stage.setScene(new Scene(loader.load()));
      stage.setTitle("Авторизация");
    } catch (IOException e) {
      log.error("Ошибка при загрузке формы регистрации", e);
    }
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MessageRequest {
    private String sender;
    private String recipient;
    private String message;
  }
}
