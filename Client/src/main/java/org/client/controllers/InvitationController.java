package org.client.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.client.diffie_hellman.DiffieHellmanProtocol;
import org.client.dto.Invitation;
import org.client.dto.InvitationRequest;
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

  public String createChat(String authToken, Label messageLabel) {
    Dialog<String> dialog = new Dialog<>();
    dialog.setTitle("Создать новый чат");
    dialog.setHeaderText("Введите имя пользователя для нового чата");

    ButtonType createButtonType = new ButtonType("Создать", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);

    dialog.getDialogPane().getButtonTypes().addAll(createButtonType, cancelButtonType);

    TextField recipientInput = new TextField();
    recipientInput.setPromptText("Имя пользователя");

    GridPane grid = new GridPane();
    grid.add(new Label("Получатель: "), 0, 0);
    grid.add(recipientInput, 1, 0);
    dialog.getDialogPane().setContent(grid);

    Platform.runLater(recipientInput::requestFocus);

    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == createButtonType) {
        String recipient = recipientInput.getText().trim();

        if (!userExists(recipient, authToken)) {
          showError(messageLabel, "Пользователь " + recipient + " не найден");
          return null;
        }

        return recipient;
      }
      return null;
    });

    Optional<String> result = dialog.showAndWait();

    return result.map(String::trim).orElse(null);
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
  public void sendInvitation(String username, String recipient, String authToken, Label messageLabel) throws JsonProcessingException {
    clearLabel(messageLabel);

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

    sendInvitationRequest(username, recipient, dh.getPublicA(), authToken, messageLabel);
  }

  @FXML
  public Path sendConfirmation(String username, String recipient, String authToken, Label messageLabel, BigInteger B) throws JsonProcessingException {
    clearLabel(messageLabel);

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
        keyService.writeFinalKey(username, recipient, key);
        newChat = createChatFilePath(username, recipient);
      } catch (IOException e) {
        log.error("Write private key error", e);
        return null;
      }

      sendInvitationRequest(username, recipient, dh.getPublicA(), authToken, messageLabel);
    } else {
      sendInvitationRequest(username, recipient, null, authToken, messageLabel);
    }

    return newChat;
  }

  private void sendInvitationRequest(String username, String recipient, BigInteger A, String authToken, Label messageLabel) throws JsonProcessingException {
    Invitation invitation = new Invitation(username, A);
    InvitationRequest invitationRequest = new InvitationRequest(recipient, invitation);
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
        showError(messageLabel, "Ошибка сервера: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Invitation sending error", e);
      showError(messageLabel, "Ошибка отправки: " + e.getMessage());
    }
  }

  public InvitationStatus processInvitation(String username, String jsonMessage) throws IOException {
    Invitation invitation = keyService.getMapper()
            .readValue(jsonMessage, Invitation.class);

    String sender = invitation.getSender();
    BigInteger B = invitation.getA();

    Path privateKeyFilePath = keyService.getPrivateKeyFilePath(username, sender);

    if (Files.exists(privateKeyFilePath)) { //Ответ на приглашение
      if (B != null) { //Принятие
        BigInteger a = keyService.readPrivateKey(privateKeyFilePath);
        var dh = new DiffieHellmanProtocol(a);

        byte[] key = dh.getKey(B);

        keyService.writeFinalKey(username, sender, key);
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
      keyService.writeInvitation(username, sender, B);

      var res = new InvitationStatus("invitation");
      res.setSender(sender);
      res.setB(B);

      return res;
    }
  }

  public Path invitationDialog(String username, String sender, BigInteger B, String authToken, Label messageLabel) {
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
      newChat = sendConfirmation(username, sender, authToken, messageLabel, finalB);
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
      return keyService.readPrivateKey(file);
    } catch (IOException e) {
      log.error("Invitation reading error", e);
      showError(messageLabel, "Ошибка чтения приглашения");
      return null;
    }
  }

  @Data
  public static class InvitationStatus {
    private final String status;
    private Path newChat = null;
    private String sender = null;
    private BigInteger B = null;
  }
}
