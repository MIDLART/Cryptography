package org.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.crypto.rc5.RC5;
import org.client.dto.ChatMessage;
import org.client.models.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Getter
@Slf4j
//@Service
public class ChatService {
  public static final String CHAT_DIR = "Client/src/main/resources/org/client/chats/";
  private final ObjectMapper mapper = new ObjectMapper();

  private final EncryptionMode encryptionMode;
  private final PackingMode packingMode;
  private final byte[] initVector;

  private static final Object FILE_LOCK = new Object();

  public ChatService(EncryptionMode encryptionMode, PackingMode packingMode, byte[] initVector) {
    this.encryptionMode = encryptionMode;
    this.packingMode = packingMode;
    this.initVector = initVector;
  }

  public static Path getChatDirectoryPath(String username) throws IOException {
    Path userChatDir = Paths.get(CHAT_DIR, username);

    if (!Files.exists(userChatDir)) {
      Files.createDirectories(userChatDir);
      log.info("Создана директория для чатов пользователя: {}", userChatDir);
    }

    return userChatDir;
  }

  public static Path getChatFilePath(String username, String chatName) throws IOException {
    Path userChatDir = getChatDirectoryPath(username);

    return userChatDir.resolve(chatName + "_chat.txt");
  }

  public Path openChat(String username, String chatName, ListView<Message> chatListView,
                       Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {
    Path chatFile = getChatFilePath(username, chatName);

    try {
      if (!Files.exists(chatFile)) {
        Files.createFile(chatFile);
      } else {
        List<String> lines;
        synchronized (FILE_LOCK) {
          lines = Files.readAllLines(chatFile);
        }

        if(!encryptionAlgorithms.containsKey(chatName)) {
          byte[] key = lines.getFirst().getBytes(StandardCharsets.UTF_8);

          var algorithm = new SymmetricAlgorithm(
                  new RC5(key, 16), encryptionMode, packingMode, initVector);
          encryptionAlgorithms.put(chatName, algorithm);
        }

        ObservableList<Message> messages = FXCollections.observableArrayList();

        for (int i = 1; i < lines.size(); i++) {
          String line = lines.get(i);
          if (line.startsWith("@me@")) {
            messages.add(new Message(line.substring(4), true));
          } else if (line.startsWith("@il@")) {
            messages.add(new Message(line.substring(4), false));
          }
        }

        Platform.runLater(() -> {
          chatListView.setItems(messages);
          chatListView.setCellFactory(lv -> new ListCell<Message>() {
            @Override
            protected void updateItem(Message msg, boolean empty) {
              super.updateItem(msg, empty);
              if (empty || msg == null) {
                setText(null);
                setGraphic(null);
                getStyleClass().clear();
              } else {
                setText(msg.getText());
                getStyleClass().clear();
                getStyleClass().add(msg.isMe() ? "message-right" : "message-left");
              }
            }
          });
        });
      }
    } catch (IOException e) {
      log.error("Ошибка работы с файлом чата", e);
      throw new IOException();
    }

    return chatFile;
  }

  public void writeMessage(Path chatFile, boolean isMe, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    if (chatFile != null && Files.exists(chatFile)) {
      message += "\n";

      Message newMessage = new Message(message, isMe);

      if (chatFile.equals(curOpenChat)) {
        Platform.runLater(() -> {
          chatListView.getItems().add(newMessage);
          chatListView.scrollTo(chatListView.getItems().size() - 1);
        });
      }

      String messageWithSender;
      if (isMe) {
        messageWithSender = "@me@" + message;
      } else {
        messageWithSender = "@il@" + message;
      }

      synchronized (FILE_LOCK) {
        Files.writeString(
                chatFile,
                messageWithSender,
                StandardOpenOption.APPEND);
      }
    }
  }

  public void meWriteMessage(Path chatFile, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    writeMessage(chatFile, true, message, chatListView, curOpenChat);
  }

  public void interlocutorWriteMessage(String username, String chatName, byte[] message,
                                       ListView<Message> chatListView, Path curOpenChat,
                                       Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {

    Path chatFile = getChatFilePath(username, chatName);

    SymmetricAlgorithm algorithm;
    if(!encryptionAlgorithms.containsKey(chatName)) {
      byte[] key;
      synchronized (FILE_LOCK) {
        try (Scanner scanner = new Scanner(chatFile)) {
          String firstLine = scanner.nextLine();
          key = firstLine.getBytes(StandardCharsets.UTF_8);
        }
      }

      algorithm = new SymmetricAlgorithm(
              new RC5(key, 16), encryptionMode, packingMode, initVector);
      encryptionAlgorithms.put(chatName, algorithm);
    } else {
      algorithm = encryptionAlgorithms.get(chatName);
    }

    CancellableCompletableFuture<byte[]> decryptFuture = algorithm.decryptAsync(message);

    byte[] decryptedMessage;
    try {
      decryptedMessage = decryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
//      showError(messageLabel, "Ошибка шифрования");
      return;
    }

    writeMessage(
            chatFile, false,
            (new String(decryptedMessage, StandardCharsets.UTF_8)),
            chatListView, curOpenChat);
  }

  public CompletableFuture<Void> interlocutorWriteMessageAsync(String username, String chatName, byte[] message,
                                                               ListView<Message> chatListView, Path curOpenChat,
                                                               Map<String, SymmetricAlgorithm> encryptionAlgorithms) {
    return CompletableFuture.runAsync(() -> {
              try {
                interlocutorWriteMessage(username, chatName, message,
                        chatListView, curOpenChat, encryptionAlgorithms);
              } catch (IOException e) {
                log.error("Interlocutor write message error {}", e.getMessage());
              }
            });
  }

  public void interlocutorParseAndWriteMessage(String username, String jsonMessage, ListView<Message> chatListView,
                                               Path curOpenChat, Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {
    ChatMessage chatMessage = mapper.readValue(jsonMessage, ChatMessage.class);

    interlocutorWriteMessageAsync(
            username, chatMessage.getSender(), chatMessage.getMessage(),
            chatListView, curOpenChat, encryptionAlgorithms);
  }
}
