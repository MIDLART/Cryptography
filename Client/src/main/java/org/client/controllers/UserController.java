package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.dto.*;
import org.client.models.Message;
import org.client.services.ChatService;
import org.client.services.FileService;
import org.client.services.MessageService;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.client.services.ChatService.CHAT_DIR;
import static org.client.services.CommonService.*;
import static org.client.services.KeyService.getInvitationFilePath;

@Log4j2
@Controller
public class UserController {
  private final Map<String, SymmetricAlgorithm> encryptionAlgorithms = new HashMap<>();
  private final EncryptionMode encryptionMode = EncryptionMode.ECB;
  private final PackingMode packingMode = PackingMode.ANSIX923;
  private final byte[] initVector =
          {(byte) 0x89, (byte) 0x01, (byte) 0x37, (byte) 0x23, (byte) 0xA0, (byte) 0xB1, (byte) 0x99, (byte) 0xE4,
           (byte) 0xDE, (byte) 0x73, (byte) 0x23, (byte) 0x5A, (byte) 0x5B, (byte) 0x52, (byte) 0x8F, (byte) 0x8B,};

  private final MessageService messageService = new MessageService();
  private final ChatService chatService = new ChatService(encryptionMode, packingMode, initVector);
  private final FileService fileService = new FileService();
  private final InvitationController invitationController = new InvitationController();

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  private ScheduledFuture<?> updateTask;

  @Setter @Getter private String authToken;
  @Setter @Getter private String username;
  @Setter @Getter private String recipientName;

  @FXML private Button button;
  @FXML private Hyperlink authLink;

  @FXML private TextField messageField;
  @FXML private Label messageLabel;
  @FXML private Label fileLabel;
  private File curAttachedFile = null;

  private Path chatFile;
  @FXML private ListView<Message> chatListView;
  @FXML private VBox chatsList;
  @FXML private HBox messageControls;


  public void initialize() {
    TextFormatter<String> textFormatter = new TextFormatter<>(change ->
            change.getControlNewText().length() > 1000 ? null : change);

    messageField.setTextFormatter(textFormatter);
  }

  public void chats() {
//    chatService.loadChatsList(chatsList, username, messageLabel);
    chatsList.getChildren().clear();

    Path userChatDir = Path.of(CHAT_DIR, username);

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

      try (Stream<Path> paths = Files.list(userChatDir)) {
        paths.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("_deferred.txt"))
                .forEach(path -> {
                  String fileName = path.getFileName().toString();
                  String sender = fileName.substring(0, fileName.length() - "_deferred.txt".length());
                  BigInteger B = invitationController.readInvitation(path, messageLabel);

                  if (B != null) {
                    addConfirmationButton(sender, B);
                  }
                });
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
  private void createChat() throws JsonProcessingException {
    String recipient = invitationController.createChat(authToken, messageLabel);
    if (recipient != null && !invitationController.checkChatExist(username, recipient, messageLabel)) {
      invitationController.sendInvitation(username, recipient, authToken, messageLabel);
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
      chatFile = chatService.openChat(username, recipientName, chatListView, encryptionAlgorithms);

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
        getMessageList(response.body());
      } else {
        showError(messageLabel, "Ошибка сервера: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Message loading error", e);
      showError(messageLabel, "Ошибка получения сообщения: " + e.getMessage());
    }
  }

  public void getMessageList(String jsonMessage) throws IOException {
    List<QueueMessage> messages = chatService.getMapper().readValue(
            jsonMessage,
            new TypeReference<List<QueueMessage>>() {}
    );

    for (QueueMessage msg : messages) {
      String type = msg.getType();
      String text = msg.getJsonText();

      if (type.equals("ChatMessage")) {
        chatService.interlocutorParseAndWriteMessage(username, text, chatListView, chatFile, encryptionAlgorithms);
      } else if (type.equals("ChatFileMessage")) {
        chatService.interlocutorParseAndWriteFileMessage(username, text, chatListView, chatFile, encryptionAlgorithms);
      } else if (type.equals("Invitation")) {
        InvitationController.InvitationStatus status = invitationController
                .processInvitation(username, text, authToken, messageLabel);

        log.info(status.getStatus());

        if (status.getStatus().equals("confirm")) {
          addChatButton(status.getNewChat());
        } else if (status.getStatus().equals("invitation")) {
          addConfirmationButton(status.getSender(), status.getB());
        }
      }
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
  private void sendMessage() {
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

    if (authToken == null || authToken.isEmpty()) {
      log.error("Токен не установлен!");
      return;
    }

    if (message != null && !message.trim().isEmpty()) {
      messageService.sendAsync(username, recipient, message,
              encryptionAlgorithms, authToken, chatFile, chatListView, messageLabel, chatService);
    }

    if (curAttachedFile != null) {
      messageService.sendFileAsync(username, recipient, curAttachedFile,
              encryptionAlgorithms, authToken, chatFile, chatListView, messageLabel, chatService);
    }

    curAttachedFile = null;
    clearLabel(fileLabel);
    messageField.clear();
  }

  @FXML
  public void attachFile() {
    curAttachedFile = fileService.attachFile(fileLabel);
  }

  private void addConfirmationButton(String sender, BigInteger B) {
    String buttonName = "Приглашение от " + sender;
    Button button = new Button(buttonName);

    button.setMaxWidth(Double.MAX_VALUE);
    button.setOnAction(e -> {
      Path newChat = invitationController.invitationDialog(username, sender, B, authToken, messageLabel);
      if (newChat != null) {
        addChatButton(newChat);
      }

      try {
        Files.delete(getInvitationFilePath(username, sender));
      } catch (IOException ex) {
        log.error("Error deleting the invitation file");
        showError(messageLabel, "Ошибка удаления файла запроса");
      }
      Platform.runLater(() -> chatsList.getChildren().remove(button));
    });

    Platform.runLater(() -> chatsList.getChildren().add(button));
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

    shutdown();
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
