package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.client.dto.ChatMessage;
import org.client.dto.MessageRequest;
import org.client.models.Message;
import org.client.services.ChatService;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.client.services.CommonService.*;

@Log4j2
@Controller
public class UserController {
  private final ChatService chatService = new ChatService();
  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private ScheduledFuture<?> updateTask;

  @Setter @Getter private String authToken;
  @Setter @Getter private String username;

  @FXML private Button button;
  @FXML private Hyperlink authLink;

  @FXML private TextField messageField;

  @FXML private Label messageLabel;

  private Path chatFile;
  @FXML private ListView<Message> chatListView;
  @FXML private VBox chatsList;
  @FXML private HBox messageControls;

  @Setter @Getter private String recipientName;

  private final ObjectMapper mapper = new ObjectMapper();

  public void chats() {
//    chatService.loadChatsList(chatsList, username, messageLabel);
    chatsList.getChildren().clear();

    Path userChatDir = Path.of(chatService.getChatDirectory(), username);

    try {
      if (!Files.exists(userChatDir)) {
        Files.createDirectories(userChatDir);
        return;
      }

      try (Stream<Path> paths = Files.list(userChatDir)) {
        paths.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("_chat.txt"))
                .forEach(this::addChatButton);
      }
    } catch (IOException e) {
      log.error("Ошибка при загрузке списка чатов", e);
      showError(messageLabel, "Ошибка загрузки списка чатов: " + e.getMessage());
    }

    startChatUpdates();
  }

  private void addChatButton(Path chatFile) {
    String fileName = chatFile.getFileName().toString();
    String chatName = fileName.replace("_chat.txt", "");

    Button chatButton = new Button(chatName);
    chatButton.setMaxWidth(Double.MAX_VALUE);
    chatButton.setOnAction(e -> {
      recipientName = chatName;
      loadChat();
    });

    Platform.runLater(() -> chatsList.getChildren().add(chatButton));
  }

  @FXML
  private void createChat() {
    String recipient = chatService.createChat(authToken, messageLabel);
    if (recipient != null) {
      recipientName = recipient;
      loadChat();
      chats();
    }
  }

  @FXML
  private void loadChat() {
    clearLabel(messageLabel);
    chatListView.getItems().clear();

    if (username == null || username.isEmpty()) {
      showError(messageLabel, "Имя пользователя не установлено");
      return;
    }

    if (recipientName == null || recipientName.isEmpty()) {
      showError(messageLabel, "Чат не выбран");
      return;
    }

    try {
      chatFile = chatService.openChat(username, recipientName, chatListView);

      messageControls.setVisible(true);

      Platform.runLater(() -> {
        if (!chatListView.getItems().isEmpty()) {
          chatListView.scrollTo(chatListView.getItems().size() - 1);
        }
      });
    } catch (IOException e) {
      showError(messageLabel, "Ошибка загрузки чата: " + e.getMessage());
    }
  }

  @FXML
  private void updateChat() {
    if (username == null || username.isEmpty()) {
      showError(messageLabel, "Имя пользователя не установлено");
      return;
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/load-message?username=" + username))
            .header("Authorization", "Bearer " + authToken)
            .GET()
            .build();

    try {
      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        List<ChatMessage> messages = mapper.readValue(
                response.body(),
                new TypeReference<List<ChatMessage>>() {}
        );

        for (ChatMessage msg : messages) {
          chatService.interlocutorWriteMessage(
                  chatService.getChatFilePath(username, msg.getSender()),
                  msg.getMessage(), chatListView, chatFile);
        }
      } else {
        showError(messageLabel, "Ошибка сервера: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Message loading error", e);
      showError(messageLabel, "Ошибка получения сообщения: " + e.getMessage());
    }
  }

  public void startChatUpdates() {
    stopChatUpdates();

    updateTask = scheduler.scheduleAtFixedRate(() ->
            Platform.runLater(this::updateChat), 0, 500, TimeUnit.MILLISECONDS);
  }

  public void stopChatUpdates() {
    if (updateTask != null) {
      updateTask.cancel(false);
    }
  }

  @FXML
  private void sendMessage() throws JsonProcessingException {
    clearLabel(messageLabel);

    if (!messageControls.isVisible()) {
      return;
    }

    String message = messageField.getText();
    String recipient = recipientName;

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
    String jsonRequest = mapper.writeValueAsString(messageRequest);

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
        chatService.meWriteMessage(chatFile, message, chatListView, chatFile);
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

      FXMLLoader loader = new FXMLLoader(url);

      Stage stage = (Stage) authLink.getScene().getWindow();

      stage.setScene(new Scene(loader.load()));
      stage.setTitle("Авторизация");
    } catch (IOException e) {
      log.error("Ошибка при загрузке формы регистрации", e);
    }
  }

  public void shutdown() {
    stopChatUpdates();

    try {
      scheduler.shutdown();
      if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

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
}
