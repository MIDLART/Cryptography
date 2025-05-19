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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.client.services.CommonService.showError;

@Log4j2
@Service
public class MessageService {
  public CompletableFuture<Void> sendAsync(String username, String recipient, String message,
                                           Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                                           Path chatFile, ListView<Message> chatListView, Label messageLabel, ChatService chatService) {
    return CompletableFuture.runAsync(() ->
            send(username, recipient, message, symmetricEncryption, authToken, chatFile, chatListView, messageLabel, chatService));
  }

  public void send(String username, String recipient, String message,
                   Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken, Path chatFile,
                   ListView<Message> chatListView, Label messageLabel, ChatService chatService) {

    CancellableCompletableFuture<byte[]> encryptFuture =
            symmetricEncryption.get(recipient).encryptAsync(message.getBytes());

    byte[] encryptedMessage;
    try {
      encryptedMessage = encryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования");
      return;
    }

    MessageRequest messageRequest =
            new MessageRequest(username, recipient, Base64.getEncoder().encodeToString(encryptedMessage));

    String jsonRequest;
    try {
      jsonRequest = chatService.getMapper().writeValueAsString(messageRequest);
    } catch (JsonProcessingException e) {
      log.error("Json processing error", e);
      return;
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

      if (response.statusCode() == 200) {
        chatService.meWriteMessage(chatFile, message, chatListView, chatFile);
      } else {
        showError(messageLabel, "Ошибка сервера: " + response.body());
      }

    } catch (IOException | InterruptedException e) {
      log.error("Message sending error", e);
      showError(messageLabel, "Ошибка отправки сообщения: " + e.getMessage());
    }
  }
}
