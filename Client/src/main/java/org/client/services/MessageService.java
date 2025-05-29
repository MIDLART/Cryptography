package org.client.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import lombok.extern.log4j.Log4j2;
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.dto.FileMessageRequest;
import org.client.dto.MessageRequest;
import org.client.models.Message;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.client.services.CommonService.showError;

@Log4j2
@Service
public class MessageService {
  private static final int CHUNK_SIZE = 64 * 1024;
  private final FileService fileService = new FileService();

  public CompletableFuture<Void> sendFileAsync(String username, String recipient, File file,
                                               Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                                               Path chatFile, ListView<Message> chatListView, Label messageLabel,
                                               ChatService chatService) {
    return CompletableFuture.runAsync(() ->
            sendFile(username, recipient, file, symmetricEncryption, authToken,
                    chatFile, chatListView, messageLabel, chatService));
  }

  public CompletableFuture<Void> sendAsync(String username, String recipient, String message,
                                           Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken,
                                           Path chatFile, ListView<Message> chatListView, Label messageLabel,
                                           ChatService chatService) {
    return CompletableFuture.runAsync(() ->
            sendMessage(username, recipient, message, symmetricEncryption, authToken,
                        chatFile, chatListView, messageLabel, chatService));
  }

  public void sendMessage(String username, String recipient, String message,
                   Map<String, SymmetricAlgorithm> symmetricEncryption, String authToken, Path chatFile,
                   ListView<Message> chatListView, Label messageLabel, ChatService chatService) {

    byte[] encryptedMessage;
    try {
      encryptedMessage = takeEncryptedMessage(recipient, message.getBytes(), symmetricEncryption);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования");
      return;
    }

    MessageRequest messageRequest =
            new MessageRequest(username, recipient, encryptedMessage);

    String jsonRequest;
    try {
      jsonRequest = chatService.getMapper().writeValueAsString(messageRequest);
    } catch (JsonProcessingException e) {
      log.error("Json processing error", e);
      return;
    }

    int statusCode = send(jsonRequest, authToken, messageLabel, "message");

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

    String fileName = file.getName();
    byte[] encryptedName;
    try {
      encryptedName = takeEncryptedMessage(recipient, fileName.getBytes(), symmetricEncryption);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования названия файла");
      return;
    }

    byte[] buffer = new byte[CHUNK_SIZE];
    UUID id = UUID.randomUUID();

    try (InputStream fileInputStream = new FileInputStream(file)) {
      int bytesRead;
      int chunkNumber = 1;
      int totalChunks = (int) Math.ceil((double) file.length() / CHUNK_SIZE);

      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
        String jsonRequest = chunkRequest(username, recipient, buffer, bytesRead, symmetricEncryption, encryptedName,
                                          messageLabel, chatService, chunkNumber, totalChunks, id);
        if (jsonRequest == null) {
          return;
        }

        int statusCode = send(jsonRequest, authToken, messageLabel, "file-message");

        if (statusCode != 200) {
          showError(messageLabel, "Ошибка отправки файла (часть " + (chunkNumber) + ")");
          return;
        }

        chunkNumber++;

        // Обновляем прогресс
      }
    } catch (IOException e) {
      log.error("File reading error", e);
      showError(messageLabel, "Ошибка чтения файла");
      return;
    }

    Path filePath = fileService.saveFile(username, recipient, file);
    if (filePath != null) {
      try {
        if (fileService.isImage(fileName)) {
          chatService.meWriteImage(chatFile, chatListView, chatFile, filePath);
        } else {
          chatService.meWriteFile(chatFile, chatListView, chatFile, filePath);
        }
      } catch (IOException e) {
        log.error("File writing error", e);
        showError(messageLabel, "Ошибка записи файла");
      }
    }
  }

  private int send(String jsonRequest, String authToken, Label messageLabel, String type) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/send-" + type))
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

  private byte[] takeEncryptedMessage(String recipient, byte[] message,
                                     Map<String, SymmetricAlgorithm> symmetricEncryption) throws ExecutionException, InterruptedException {
    CancellableCompletableFuture<byte[]> encryptFuture =
            symmetricEncryption.get(recipient).encryptAsync(message);

    return encryptFuture.get();
  }

  private String chunkRequest(String username, String recipient, byte[] buffer, int bytesRead,
                              Map<String, SymmetricAlgorithm> symmetricEncryption, byte[] encryptedName,
                              Label messageLabel, ChatService chatService, int chunkNumber, int totalChunks, UUID id) {

    byte[] chunkData = bytesRead == CHUNK_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);

    byte[] encryptedChunk;
    try {
      encryptedChunk = takeEncryptedMessage(recipient, chunkData, symmetricEncryption);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования файла (часть " + (chunkNumber) + ")");
      return null;
    }

    FileMessageRequest chunkRequest = new FileMessageRequest(
            username, recipient, id, encryptedName, encryptedChunk, chunkNumber, totalChunks);

    String jsonRequest;
    try {
      jsonRequest = chatService.getMapper().writeValueAsString(chunkRequest);
    } catch (JsonProcessingException e) {
      log.error("Json processing error", e);
      return null;
    }

    return jsonRequest;
  }
}
