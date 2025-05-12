package org.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.client.dto.ChatMessage;
import org.client.models.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

@Getter
@Slf4j
@Service
public class ChatService {
  public static final String CHAT_DIR = "Client/src/main/resources/org/client/chats/";
  private final ObjectMapper mapper = new ObjectMapper();

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

  public Path openChat(String username, String chatName, ListView<Message> chatListView, Map<String, byte[]> keys) throws IOException {
    Path chatFile = getChatFilePath(username, chatName);

    try {
      if (!Files.exists(chatFile)) {
        Files.createFile(chatFile);
      } else {
        List<String> lines = Files.readAllLines(chatFile);

        if(!keys.containsKey(chatName)) {
          keys.put(chatName, lines.getFirst().getBytes());
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

      Files.writeString(
              chatFile,
              messageWithSender,
              StandardOpenOption.APPEND);
    }
  }

  public void meWriteMessage(Path chatFile, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    writeMessage(chatFile, true, message, chatListView, curOpenChat);
  }

  public void interlocutorWriteMessage(Path chatFile, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    writeMessage(chatFile, false, message, chatListView, curOpenChat);
  }

  public void interlocutorParseAndWriteMessage(String username, String jsonMessage, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    ChatMessage chatMessage = mapper.readValue(jsonMessage, ChatMessage.class);

    interlocutorWriteMessage(
            getChatFilePath(username, chatMessage.getSender()),
            chatMessage.getMessage(), chatListView, curOpenChat);
  }

//  public void loadChatsList(VBox chatsList, String username, Label messageLabel) {
//    chatsList.getChildren().clear();
//
//    try (Stream<Path> paths = Files.list(Path.of(chatDirectory + username))) {
//      paths.filter(Files::isRegularFile)
//              .filter(p -> p.getFileName().toString().endsWith("_chat.txt"))
//              .forEach(this::addChatButton);
//    } catch (IOException e) {
//      log.error("Ошибка при загрузке списка чатов", e);
//      showError(messageLabel, "Ошибка загрузки списка чатов: " + e.getMessage());
//    }
//  }
}
