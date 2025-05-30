package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.client.crypto.SymmetricAlgorithm;
import org.client.dto.*;
import org.client.models.ChatSettings;
import org.client.models.Message;
import org.client.services.ChatService;
import org.client.services.FileService;
import org.client.services.KeyService;
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
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.client.services.ChatService.CHAT_DIR;
import static org.client.services.ChatService.getChatFilePath;
import static org.client.services.CommonService.*;
import static org.client.services.KeyService.getInvitationFilePath;

@Log4j2
@Controller
public class UserController {
  private final Map<String, SymmetricAlgorithm> encryptionAlgorithms = new HashMap<>();

  private final MessageService messageService = new MessageService();
  private final ChatService chatService = new ChatService();
  private final FileService fileService = new FileService();
  private final KeyService keyService = new KeyService();
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
  @FXML private HBox chatControls;
  @FXML private Label chatTitleLabel;


  public void initialize() {
    TextFormatter<String> textFormatter = new TextFormatter<>(change ->
            change.getControlNewText().length() > 1000 ? null : change);

    messageField.setTextFormatter(textFormatter);
  }

  public void chats() {
    chatsList.getChildren().clear();

    Path userChatDir = Path.of(CHAT_DIR, username);

    try {
      if (!Files.exists(userChatDir)) {
        Files.createDirectories(userChatDir);
      }

      try (Stream<Path> paths = Files.list(userChatDir)) {
        paths.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("_chat.txt"))
                .forEach(this::addChatButton);
      }

      try (Stream<Path> paths = Files.list(userChatDir)) {
        paths.filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().endsWith("_deferred.json"))
                .forEach(path -> {
                  String fileName = path.getFileName().toString();
                  String sender = fileName.substring(0, fileName.length() - "_deferred.json".length());
                  BigInteger B = invitationController.readInvitation(path, messageLabel);

                  ChatSettings chatSettings = invitationController.readChatSettings(path, sender);

                  if (B != null && chatSettings != null) {
                    addConfirmationButton(chatSettings, B);
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
    ChatSettings settings = invitationController.createChat(authToken, messageLabel);
    if (settings != null && !invitationController.checkChatExist(username, settings.getRecipient(), messageLabel)) {
      invitationController.sendInvitation(username, settings, authToken, messageLabel);
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

      if (!keyService.isKeyNull(username, recipientName)) {
        messageControls.setVisible(true);
        chatTitleLabel.setVisible(false);
      } else {
        messageControls.setVisible(false);
        chatTitleLabel.setVisible(true);
      }

      chatControls.setVisible(true);

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
        InvitationController.InvitationStatus status = invitationController.processInvitation(username, text);

        log.info(status.getStatus());

        if (status.getStatus().equals("confirm")) {
          addChatButton(status.getNewChat());
        } else if (status.getStatus().equals("invitation")) {
          addConfirmationButton(status.getSettings(), status.getB());
        }
      } else if (type.equals("Delete")) {
        DeleteRequest request = chatService.getMapper().readValue(text, DeleteRequest.class);

        if (request.getForBoth()) {
          deleteAndClearChat();
        } else {
          try {
            keyService.updateKeyInConfig(request.getRecipient(), request.getSender(), null);
          } catch (IOException e) {
            log.error("Error updating key", e);
          }

          showSuccess(messageLabel, "Удалён чат c " + request.getSender());
        }

        if (chatFile == getChatFilePath(username, request.getSender())) {
          loadChat();
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

  private void addConfirmationButton(ChatSettings settings, BigInteger B) {
    String sender = settings.getRecipient();

    String buttonName = "Приглашение от " + sender;
    Button button = new Button(buttonName);

    button.setMaxWidth(Double.MAX_VALUE);
    button.setOnAction(e -> {
      Path newChat = invitationController.invitationDialog(username, settings, B, authToken, messageLabel);
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

//  @FXML
//  private void connectOrDisconnect() {
//    if (!keyService.isKeyNull(username, recipientName)) {
//      try {
//        keyService.updateKeyInConfig(username, recipientName, null);
//      } catch (IOException e) {
//        log.error("Error updating key", e);
//      }
//    } else {
//      ChatSettings settings;
//      try {
//        settings = keyService.readChatSettings(username, recipientName);
//        invitationController.sendInvitation(username, settings, authToken, messageLabel);
//      } catch (IOException e) {
//        log.error("Error reading chat settings or sending invitation", e);
//      }
//
//      chatControls.setVisible(false);
//    }
//  }

  @FXML
  private void deleteChat() {
    if (username == null || recipientName == null) {
      showError(messageLabel, "Не выбран чат для удаления");
      return;
    }

    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Подтверждение удаления");
    confirmation.setHeaderText("Выберите тип удаления чата с " + recipientName);

    ToggleGroup group = new ToggleGroup();
    RadioButton forMeOnly = new RadioButton("Только у меня");
    RadioButton forBoth = new RadioButton("У всех участников");
    forMeOnly.setToggleGroup(group);
    forBoth.setToggleGroup(group);
    forMeOnly.setSelected(true);

    VBox vbox = new VBox(10, forMeOnly, forBoth);
    vbox.setPadding(new Insets(20));
    confirmation.getDialogPane().setContent(vbox);

    confirmation.getButtonTypes().clear();
    ButtonType deleteButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
    confirmation.getButtonTypes().addAll(deleteButton, cancelButton);

    Optional<ButtonType> result = confirmation.showAndWait();
    if (result.isPresent() && result.get() == deleteButton) {
      try {
        boolean deleteForAll = forBoth.isSelected();

        DeleteRequest messageRequest = new DeleteRequest(username, recipientName, deleteForAll);
        String jsonRequest = chatService.getMapper().writeValueAsString(messageRequest);

        messageService.send(jsonRequest, authToken, messageLabel, "delete-chat");

        deleteAndClearChat();

        showSuccess(messageLabel, deleteForAll
                ? "Запрос на удаление чата у обоих участников отправлен"
                : "Чат успешно удален (только у вас)");

      } catch (Exception e) {
        showError(messageLabel, "Ошибка при удалении чата: " + e.getMessage());
        log.error("Delete chat error", e);
      }
    }
  }

  private void deleteAndClearChat() {
    chatService.deleteChat(username, recipientName);
    encryptionAlgorithms.remove(recipientName);

    chatListView.getItems().clear();
    messageControls.setVisible(false);
    chatControls.setVisible(false);
    removeChatButton(recipientName);
    recipientName = null;
    chatFile = null;
  }

  private void removeChatButton(String chatName) {
    Platform.runLater(() -> {
      chatsList.getChildren().removeIf(node -> {
        if (node instanceof Button) {
          Button button = (Button) node;
          return button.getText().equals(chatName);
        }
        return false;
      });
    });
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
      if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
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
