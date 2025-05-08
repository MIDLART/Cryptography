package org.client.services;

import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
public class ChatService {
  private final String chatDirectory = "Client/src/main/resources/org/client/chats/";

  public Path openChat(String username, TextArea chatArea) throws IOException {
    Path userChatDir = Paths.get(chatDirectory, username);

    if (!Files.exists(userChatDir)) {
      Files.createDirectories(userChatDir);
      log.info("Создана директория для чатов пользователя: {}", userChatDir);
    }

    Path chatFile = userChatDir.resolve(username + "_chat.txt");

    try {
      if (!Files.exists(chatFile)) {
        Files.createFile(chatFile);
      } else {
        String content = Files.readString(chatFile);
        chatArea.setText(content);
      }
    } catch (IOException e) {
      log.error("Ошибка работы с файлом чата", e);
      throw new IOException();
    }

    return chatFile;
  }

  public void writeMessage(Path chatFile, String message, TextArea chatArea) throws IOException {
    if (chatFile != null && Files.exists(chatFile)) {
      message += "\n";

      chatArea.appendText(message);

      Files.writeString(
              chatFile,
              message,
              StandardOpenOption.APPEND);
    }
  }
}
