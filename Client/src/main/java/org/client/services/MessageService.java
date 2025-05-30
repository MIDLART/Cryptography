package org.client.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
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

    int statusCode = send(jsonRequest, authToken, messageLabel, "send-message");

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
    IntegerProperty progress = new SimpleIntegerProperty(0);

    byte[] encryptedName;
    try {
      encryptedName = takeEncryptedMessage(recipient, fileName.getBytes(), symmetricEncryption);
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования названия файла");
      return;
    }

    UUID id = UUID.randomUUID();
    String encryptedFileName;
    try {
      encryptedFileName = fileService.getFileDirectoryPath(username, recipient).toString() + "/" + id;
    } catch (IOException e) {
      log.error("Encrypted file name error", e);
      return;
    }

    File encryptedFile = new File(encryptedFileName);

    CancellableCompletableFuture<Void> encryptFuture =
            symmetricEncryption.get(recipient).encryptAsync(file.toString(), encryptedFileName);

    Path filePath = fileService.saveFile(username, recipient, file);
    if (filePath != null) {
      try {
        if (fileService.isImage(fileName)) {
          chatService.meWriteImage(chatFile, chatListView, chatFile, filePath, progress, encryptFuture, id);
        } else {
          chatService.meWriteFile(chatFile, chatListView, chatFile, filePath, progress, encryptFuture, id);
        }
      } catch (IOException e) {
        log.error("File writing error", e);
        showError(messageLabel, "Ошибка записи файла");
      }
    }

    try {
      while (!encryptFuture.isDone()) {
        sleep(100);
        progress.set((int) encryptFuture.getProgress());
      }
    } catch (InterruptedException e) {
      log.error("Encryption thread interrupted", e);
    }

    try {
      encryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      showError(messageLabel, "Ошибка шифрования файла");
      return;
    }

    byte[] buffer = new byte[CHUNK_SIZE];
    progress.set(0);

    try (InputStream fileInputStream = new FileInputStream(encryptedFile)) {
      int bytesRead;
      int chunkNumber = 1;
      int totalChunks = (int) Math.ceil((double) encryptedFile.length() / CHUNK_SIZE);

      while ((bytesRead = fileInputStream.read(buffer)) != -1) {
        String jsonRequest = chunkRequest(username, recipient, buffer, encryptedName, bytesRead,
                                          chatService, chunkNumber, totalChunks, id);
        if (jsonRequest == null) {
          return;
        }

        int statusCode = send(jsonRequest, authToken, messageLabel, "send-file-message");

        if (statusCode != 200) {
          showError(messageLabel, "Ошибка отправки файла (часть " + (chunkNumber) + ")");
          return;
        }

        chunkNumber++;
        progress.set((int) (((double) chunkNumber / totalChunks) * 100));
      }
    } catch (IOException e) {
      log.error("File reading error", e);
      showError(messageLabel, "Ошибка чтения файла");
      return;
    }

    try {
      Files.delete(encryptedFile.toPath());
    } catch (IOException e) {
      log.error("File deletion error", e);
    }
  }

  public int send(String jsonRequest, String authToken, Label messageLabel, String type) {
    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/" + type))
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

  private String chunkRequest(String username, String recipient, byte[] buffer, byte[] encryptedName,
                              int bytesRead, ChatService chatService, int chunkNumber, int totalChunks, UUID id) {

    byte[] chunkData = bytesRead == CHUNK_SIZE ? buffer : Arrays.copyOf(buffer, bytesRead);

    FileMessageRequest chunkRequest = new FileMessageRequest(
            username, recipient, id, encryptedName, chunkData, chunkNumber, totalChunks);

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
