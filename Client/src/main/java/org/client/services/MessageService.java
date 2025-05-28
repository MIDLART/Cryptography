package org.client.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.extern.log4j.Log4j2;
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.dto.MessageRequest;
import org.client.models.Message;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.client.services.CommonService.showError;

@Log4j2
@Service
public class MessageService {
  public CompletableFuture<Void> sendFileAsync(String username, String recipient, File file,
                                           Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                                           Path chatFile, ListView<Message> chatListView, Label messageLabel, ChatService chatService) {
    return CompletableFuture.runAsync(() ->
            sendFile(username, recipient, file, symmetricEncryption, authToken, chatFile, chatListView, messageLabel, chatService));
  }

  public CompletableFuture<Void> sendAsync(String username, String recipient, String message,
                                           Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                                           Path chatFile, ListView<Message> chatListView, Label messageLabel, ChatService chatService) {
    return CompletableFuture.runAsync(() ->
            sendMessage(username, recipient, message, symmetricEncryption, authToken,
                        chatFile, chatListView, messageLabel, chatService));
  }

  public void sendMessage(String username, String recipient, String message,
                   Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken, Path chatFile,
                   ListView<Message> chatListView, Label messageLabel, ChatService chatService) {

    int statusCode = send(username, recipient, message.getBytes(),
                          symmetricEncryption, authToken, messageLabel, chatService);

    if (statusCode == 200) {
      try {
        chatService.meWriteMessage(chatFile, message, chatListView, chatFile);
      } catch (IOException e) {
        log.error("Message write error", e);
        showError(messageLabel, "Ошибка записи сообщения");
      }
    } else if (statusCode != 0) {
      showError(messageLabel, "Ошибка сервера");
    }
  }

  public void sendFile(String username, String recipient, File file,
                       Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken, Path chatFile,
                       ListView<Message> chatListView, Label messageLabel, ChatService chatService) {

    byte[] fileBytes;
    try {
      fileBytes = Files.readAllBytes(file.toPath());
    } catch (IOException e) {
      log.error("File reading error", e);
      return;
    }

    int statusCode = send(username, recipient, fileBytes, symmetricEncryption, authToken, messageLabel, chatService);

    if (statusCode == 200) {
      //TODO
    } else if (statusCode != 0) {
      showError(messageLabel, "Ошибка сервера" );
    }
  }

  public int send(String username, String recipient, byte[] message,
                  Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                  Label messageLabel, ChatService chatService) {

    CancellableCompletableFuture<byte[]> encryptFuture =
            symmetricEncryption.get(recipient).encryptAsync(message);

    byte[] encryptedMessage;
    try {
      encryptedMessage = encryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования");
      return 0;
    }

//    MessageRequest messageRequest =
//            new MessageRequest(username, recipient, Base64.getEncoder().encodeToString(encryptedMessage));

    MessageRequest messageRequest =
            new MessageRequest(username, recipient, encryptedMessage);

    String jsonRequest;
    try {
      jsonRequest = chatService.getMapper().writeValueAsString(messageRequest);
    } catch (JsonProcessingException e) {
      log.error("Json processing error", e);
      return 0;
    }

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/send-message"))
            .header("Authorization", "Bearer " + authToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
            .build();

    try {
      HttpResponse<String> response = HttpClient.newHttpClient()
              .send(request, HttpResponse.BodyHandlers.ofString());

      return response.statusCode();

    } catch (IOException | InterruptedException e) {
      log.error("Message sending error", e);
      showError(messageLabel, "Ошибка отправки сообщения: " + e.getMessage());
    }

    return 0;
  }
}
