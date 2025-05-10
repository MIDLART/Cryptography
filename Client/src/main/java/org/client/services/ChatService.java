package org.client.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.client.models.Message;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.client.services.CommonService.showError;

@Getter
@Slf4j
@Service
public class ChatService {
  private final String chatDirectory = "Client/src/main/resources/org/client/chats/";
  private final ObjectMapper mapper = new ObjectMapper();

  public Path getChatFilePath(String username, String chatName) throws IOException {
    Path userChatDir = Paths.get(chatDirectory, username);

    if (!Files.exists(userChatDir)) {
      Files.createDirectories(userChatDir);
      log.info("Создана директория для чатов пользователя: {}", userChatDir);
    }

    return userChatDir.resolve(chatName + "_chat.txt");
  }

  public Path openChat(String username, String chatName, ListView<Message> chatListView) throws IOException {
    Path chatFile = getChatFilePath(username, chatName);

    try {
      if (!Files.exists(chatFile)) {
        Files.createFile(chatFile);
      } else {
        List<String> lines = Files.readAllLines(chatFile);
        ObservableList<Message> messages = FXCollections.observableArrayList();

        for (String line : lines) {
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
      jsonRequest = mapper.writeValueAsString(requestMap);
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
