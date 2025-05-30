package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.diffie_hellman.DiffieHellmanProtocol;
import org.client.dto.Invitation;
import org.client.dto.InvitationRequest;
import org.client.enums.Algorithm;
import org.client.models.ChatSettings;
import org.client.services.KeyService;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.client.services.ChatService.createChatFilePath;
import static org.client.services.ChatService.getChatFilePath;
import static org.client.services.CommonService.*;
import static org.client.services.CommonService.showError;

@Log4j2
@Controller
public class InvitationController {
  private final KeyService keyService = new KeyService();

  public ChatSettings createChat(String authToken, Label messageLabel) {
    Dialog<ChatSettings> dialog = new Dialog<>();
    dialog.setTitle("Создать новый чат");
    dialog.setHeaderText("Настройки нового чата");

    ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

    TextField recipientInput = new TextField();
    recipientInput.setPromptText("Имя пользователя");

    ComboBox<Algorithm> algorithmCombo = new ComboBox<>(
            FXCollections.observableArrayList(Algorithm.values()));
    algorithmCombo.getSelectionModel().selectFirst();

    ComboBox<EncryptionMode> encryptionCombo = new ComboBox<>(
            FXCollections.observableArrayList(EncryptionMode.values()));
    encryptionCombo.getSelectionModel().selectFirst();

    ComboBox<PackingMode> packingCombo = new ComboBox<>(
            FXCollections.observableArrayList(PackingMode.values()));
    packingCombo.getSelectionModel().selectFirst();

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    grid.add(new Label("Получатель:"), 0, 0);
    grid.add(recipientInput, 1, 0);
    grid.add(new Label("Алгоритм:"), 0, 1);
    grid.add(algorithmCombo, 1, 1);
    grid.add(new Label("Режим шифрования:"), 0, 2);
    grid.add(encryptionCombo, 1, 2);
    grid.add(new Label("Режим заполнения:"), 0, 3);
    grid.add(packingCombo, 1, 3);

    dialog.getDialogPane().setContent(grid);
    Platform.runLater(recipientInput::requestFocus);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == createButtonType) {
        String recipient = recipientInput.getText().trim();

        if (!userExists(recipient, authToken)) {
          showError(messageLabel, "Пользователь " + recipient + " не найден");
          return null;
        }

        return new ChatSettings(
                recipient,
                algorithmCombo.getValue(),
                encryptionCombo.getValue(),
                packingCombo.getValue()
        );
      }
      return null;
    });

    Optional<ChatSettings> result = dialog.showAndWait();
    return result.orElse(null);
  }

  private boolean userExists(String name, String authToken) {
    String jsonRequest;
    try {
      Map<String, String> requestMap = new HashMap<>();
      requestMap.put("username", name);
      jsonRequest = keyService.getMapper().writeValueAsString(requestMap);
    } catch (JsonProcessingException e) {
      return false;
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/find-user"))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

    HttpResponse<String> response;
    try {
      response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException | InterruptedException e) {
      log.error("Search error", e);
      return false;
    }

    return response.statusCode() == 200;
  }

  @FXML
  public void sendInvitation(String username, ChatSettings settings, String authToken, Label messageLabel) throws JsonProcessingException {
    clearLabel(messageLabel);

    String recipient = settings.getRecipient();

    if (recipient == null || recipient.trim().isEmpty()) {
      showError(messageLabel, "Укажите получателя");
      return;
    }

    if (authToken == null || authToken.isEmpty()) {
      log.error("Токен не установлен!");
      return;
    }

    var dh = new DiffieHellmanProtocol();

    BigInteger a = dh.getA();

    try {
      keyService.writePrivateKey(username, recipient, a);
    } catch (IOException e) {
      log.error("Write private key error", e);
      showError(messageLabel, "Ошибка записи ключа: " + e.getMessage());
      return;
    }

    sendInvitationRequest(username, settings, dh.getPublicA(), authToken, messageLabel);
  }

  @FXML
  public Path sendConfirmation(String username, ChatSettings settings, String authToken, Label messageLabel, BigInteger B) throws JsonProcessingException {
    clearLabel(messageLabel);

    String recipient = settings.getRecipient();

    if (recipient == null || recipient.trim().isEmpty()) {
      showError(messageLabel, "Укажите получателя");
      return null;
    }

    Path newChat = null;
    if (B != null) {
      if (authToken == null || authToken.isEmpty()) {
        log.error("Токен не установлен!");
        return null;
      }

      var dh = new DiffieHellmanProtocol();

      byte[] key = dh.getKey(B);

      try {
        keyService.writeConfig(username, recipient, key, settings);
        newChat = createChatFilePath(username, recipient);
      } catch (IOException e) {
        log.error("Write private key error", e);
        return null;
      }

      sendInvitationRequest(username, settings, dh.getPublicA(), authToken, messageLabel);
    } else {
      sendInvitationRequest(username, settings, null, authToken, messageLabel);
    }

    return newChat;
  }

  private void sendInvitationRequest(String username, ChatSettings settings, BigInteger A, String authToken, Label messageLabel) throws JsonProcessingException {
    Invitation invitation = new Invitation(username, A);
    InvitationRequest invitationRequest = new InvitationRequest(settings, invitation);
    String jsonRequest = keyService.getMapper().writeValueAsString(invitationRequest);

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/send-invitation"))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

    try {
      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() == 200) {
        showSuccess(messageLabel, "Отправлено");
      } else {
        showError(messageLabel, "Ошибка сервера " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Invitation sending error", e);
      showError(messageLabel, "Ошибка отправки " + e.getMessage());
    }
  }

  public InvitationStatus processInvitation(String username, String jsonMessage) throws IOException {
    InvitationRequest invitationRequest = keyService.getMapper().readValue(jsonMessage, InvitationRequest.class);
    ChatSettings settings = invitationRequest.getChatSettings();
    Invitation invitation = invitationRequest.getInvitation();

    String sender = invitation.getSender();
    BigInteger B = invitation.getA();

    settings.setRecipient(sender);

    Path privateKeyFilePath = keyService.getPrivateKeyFilePath(username, sender);

    if (Files.exists(privateKeyFilePath)) { //Ответ на приглашение
      if (B != null) { //Принятие
        BigInteger a = keyService.readPrivateKey(privateKeyFilePath);
        var dh = new DiffieHellmanProtocol(a);

        byte[] key = dh.getKey(B);

        keyService.writeConfig(username, sender, key, settings);
        Path newChat = createChatFilePath(username, sender);

        log.info("Приглашение для {} принято", sender);
        Files.delete(privateKeyFilePath);

        var res = new InvitationStatus("confirm");
        res.setNewChat(newChat);
        return res;
      } else { //Отказ
        log.info("Приглашение для {} отклонено", sender);
        Files.delete(privateKeyFilePath);
        return new InvitationStatus("reject");
      }
    } else { //Приглашение
      keyService.writeInvitation(username, sender, B, settings);

      var res = new InvitationStatus("invitation");
      res.setSettings(settings);
      res.setB(B);

      return res;
    }
  }

  public Path invitationDialog(String username, ChatSettings settings, BigInteger B, String authToken, Label messageLabel) {
    String sender = settings.getRecipient();

    log.info("Получено приглашение от {}", sender);

    Dialog<Boolean> dialog = new Dialog<>();
    dialog.setTitle("Приглашение");
    dialog.setHeaderText("Принять приглашение от " + sender + "?");

    ButtonType createButtonType = new ButtonType("Принять", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Отклонить", ButtonBar.ButtonData.CANCEL_CLOSE);

    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

    dialog.setResultConverter(dialogButton ->
            dialogButton == createButtonType);

    Optional<Boolean> result = dialog.showAndWait();

    BigInteger finalB;
    if (result.orElse(false)) {
      finalB = B;
    } else {
      finalB = null;
    }

    Path newChat = null;
    try {
      newChat = sendConfirmation(username, settings, authToken, messageLabel, finalB);
    } catch (JsonProcessingException e) {
      log.error("Send confirmation error", e);
      showError(messageLabel, "Ошибка отправки подтверждения");
    }

    return newChat;
  }

  public boolean checkChatExist(String username, String chatName, Label messageLabel) {
    try {
      if (Files.exists(getChatFilePath(username, chatName))) {
        showError(messageLabel, "Чат уже существует");
        return true;
      }

      if (Files.exists(keyService.getPrivateKeyFilePath(username, chatName))) {
        showError(messageLabel, "Приглашение уже отправлено");
        return true;
      }

      Path deferredInvitation = keyService.getInvitationFilePath(username, chatName);
      if (Files.exists(deferredInvitation)) {
//        BigInteger B = keyService.readInvitation(deferredInvitation);
//        newChat = sendConfirmation(username, sender, authToken, messageLabel, finalB);
        showError(messageLabel, "Приглашение уже ожидает вашего ответа");
        return true;
      }

    } catch (IOException e) {
      log.error("Check chat error", e);
      showError(messageLabel, "Ошибка проверки существования файла");
      return true;
    }

    return false;
  }

  public BigInteger readInvitation(Path file, Label messageLabel) {
    try {
      return keyService.readInvitationPrivateKey(file);
    } catch (IOException e) {
      log.error("Invitation reading error", e);
      showError(messageLabel, "Ошибка чтения приглашения");
      return null;
    }
  }

  public ChatSettings readChatSettings(Path file, String chatName) {
    try {
      return keyService.readChatSettings(file, chatName);
    } catch (IOException e) {
      log.error("Invitation reading settings error", e);
      return null;
    }
  }

  @Data
  public static class InvitationStatus {
    private final String status;
    private Path newChat = null;
    private ChatSettings settings = null;
    private BigInteger B = null;
  }
}
