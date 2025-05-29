package org.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.crypto.rc5.RC5;
import org.client.dto.ChatFileMessage;
import org.client.dto.ChatMessage;
import org.client.enums.MessageType;
import org.client.models.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;

@Getter
@Slf4j
//@Service
public class ChatService {
  public static final String CHAT_DIR = "Client/src/main/resources/org/client/chats/";
  private final ObjectMapper mapper = new ObjectMapper();

  private final FileService fileService = new FileService();
  private final Map<UUID, Integer> filesProgress = new HashMap<>();

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
            messages.add(new Message(line.substring(4), MessageType.MY_MESSAGE));
          } else if (line.startsWith("@il@")) {
            messages.add(new Message(line.substring(4), MessageType.INTERLOCUTOR_MESSAGE));
          } else if (line.startsWith("@mi@") || line.startsWith("@ii@")) {
            boolean isMe = line.startsWith("@mi@");
            Path filePath = Paths.get(line.substring(4));
            if (Files.exists(filePath)) {
              messages.add(new Message(
                      isMe ? MessageType.MY_IMAGE : MessageType.INTERLOCUTOR_IMAGE,
                      filePath, null)
              );
            }
          } else if (line.startsWith("@mf@") || line.startsWith("@if@")) {
            boolean isMe = line.startsWith("@mf@");
            Path filePath = Paths.get(line.substring(4));
            if (Files.exists(filePath)) {
              messages.add(new Message(
                      isMe ? MessageType.MY_FILE : MessageType.INTERLOCUTOR_FILE,
                      filePath, null)
              );
            }
          }
        }

        Platform.runLater(() -> {
          chatListView.setItems(messages);
          chatListView.setCellFactory(lv -> new MessageCell());
        });
      }
    } catch (IOException e) {
      log.error("Ошибка работы с файлом чата", e);
      throw new IOException();
    }

    return chatFile;
  }

  public class MessageCell extends ListCell<Message> {
    @Override
    protected void updateItem(Message msg, boolean empty) {
      super.updateItem(msg, empty);

      if (empty || msg == null) {
        setText(null);
        setGraphic(null);
        getStyleClass().clear();
        return;
      }

      getStyleClass().clear();
      getStyleClass().add(msg.isMe() ? "message-right" : "message-left");

      if (msg.isImage()) {
        try {
          VBox container = new VBox(5);
          container.setAlignment(msg.isMe() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

          Image image = new Image(msg.getFilePath().toUri().toString());
          ImageView imageView = new ImageView(image);
          imageView.setFitWidth(200);
          imageView.setPreserveRatio(true);

          container.getChildren().add(imageView);

          if (msg.progressProperty() != null) {
            progressCheck(container, msg);
          }

          setGraphic(container);
          setText(null);
        } catch (Exception e) {
          setText("[Ошибка загрузки изображения]");
          setGraphic(null);
        }
      } else if (msg.isFile()) {
        VBox mainContainer = new VBox(5);
        mainContainer.setAlignment(msg.isMe() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        HBox fileContainer = new HBox(5);
        fileContainer.setAlignment(msg.isMe() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label fileLabel = new Label(msg.getFilePath().getFileName().toString());
        Button downloadBtn = new Button("Открыть");
        downloadBtn.getStyleClass().add("download-button");

        downloadBtn.setOnAction(e -> {
          try {
            ProcessBuilder pb;
            String os = System.getProperty("os.name").toLowerCase();
            String filePath = msg.getFilePath().toFile().getAbsolutePath();

            if (os.contains("win")) {
              pb = new ProcessBuilder("cmd", "/c", "start", "\"DummyTitle\"", filePath);
            } else if (os.contains("mac")) {
              pb = new ProcessBuilder("open", filePath);
            } else if (os.contains("nix") || os.contains("nux")) {
              pb = new ProcessBuilder("xdg-open", filePath);
            } else {
              log.warn("Unsupported operating system. Cannot open file.");
              return;
            }
            pb.start();
          } catch (IOException ex) {
            log.error("Download failed", ex);
          }
        });

        fileContainer.getChildren().addAll(fileLabel, downloadBtn);
        mainContainer.getChildren().add(fileContainer);

        if (msg.progressProperty() != null) {
          progressCheck(mainContainer, msg);
        }

        setGraphic(mainContainer);
        setText(null);
      } else {
        setText(msg.getText());
        setGraphic(null);
      }
    }
  }

  private void progressCheck(VBox container, Message msg) {
    ProgressBar progressBar = new ProgressBar();
    progressBar.setPrefWidth(200);
    progressBar.setPrefHeight(5);
    progressBar.progressProperty().bind(
            msg.progressProperty().divide(100.0)
    );

    progressBar.visibleProperty().bind(
            msg.progressProperty().lessThan(100)
    );

    container.getChildren().add(progressBar);
  }

  public void writeMessage(Path chatFile, MessageType type, String message, Path filePath,
                           ListView<Message> chatListView, Path curOpenChat, IntegerProperty progress) throws IOException {

    if (chatFile != null && Files.exists(chatFile)) {
      message += "\n";

      Message newMessage = new Message(message, type, filePath, progress);

      if (chatFile.equals(curOpenChat)) {
        Platform.runLater(() -> {
          chatListView.getItems().add(newMessage);
          chatListView.scrollTo(chatListView.getItems().size() - 1);
        });
      }

      String messageWithSender = addPrefix(type, message);

      synchronized (FILE_LOCK) {
        Files.writeString(
                chatFile,
                messageWithSender,
                StandardOpenOption.APPEND);
      }
    }
  }

  public void meWriteMessage(Path chatFile, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    writeMessage(chatFile, MessageType.MY_MESSAGE, message, null, chatListView, curOpenChat, null);
  }

  public void interlocutorWriteMessage(String username, String chatName, byte[] message,
                                       ListView<Message> chatListView, Path curOpenChat,
                                       Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {

    Path chatFile = getChatFilePath(username, chatName);

    SymmetricAlgorithm algorithm = getAlgorithm(chatName, chatFile, encryptionAlgorithms);

    CancellableCompletableFuture<byte[]> decryptFuture = algorithm.decryptAsync(message);

    byte[] decryptedMessage;
    try {
      decryptedMessage = decryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      return;
    }

    writeMessage(
            chatFile, MessageType.INTERLOCUTOR_MESSAGE,
            (new String(decryptedMessage, StandardCharsets.UTF_8)),
            null, chatListView, curOpenChat, null);
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

  public void meWriteFile(Path chatFile, ListView<Message> chatListView, Path curOpenChat,
                          Path filePath, IntegerProperty progress) throws IOException {
    writeMessage(chatFile, MessageType.MY_FILE, filePath.toString(), filePath, chatListView, curOpenChat, progress);
  }

  public void meWriteImage(Path chatFile, ListView<Message> chatListView, Path curOpenChat,
                           Path filePath, IntegerProperty progress) throws IOException {
    writeMessage(chatFile, MessageType.MY_IMAGE, filePath.toString(), filePath, chatListView, curOpenChat, progress);
  }

  public void interlocutorWriteFileMessage(String username, String chatName, UUID fileId,
                                           byte[] fileName, byte[] fileContent, int chunkNumber,
                                           int totalChunks, ListView<Message> chatListView,
                                           Path curOpenChat, Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {

    Path chatFile = getChatFilePath(username, chatName);

    SymmetricAlgorithm algorithm = getAlgorithm(chatName, chatFile, encryptionAlgorithms);

    CancellableCompletableFuture<byte[]> decryptFuture = algorithm.decryptAsync(fileName);

    byte[] decryptedFileName;
    try {
      decryptedFileName = decryptFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      log.error("Encryption thread interrupted", e);
      return;
    }

    Path tmpFile = fileService.chunkRec(username, chatName, fileContent, chunkNumber, fileId);
    filesProgress.merge(fileId, 1, (oldValue, value) -> oldValue + 1);

    if (filesProgress.get(fileId) == totalChunks) {
      Path finalFile = fileService.createFile(
              username, chatName, (new String(decryptedFileName, StandardCharsets.UTF_8)));

      IntegerProperty progress = new SimpleIntegerProperty(0);

      CancellableCompletableFuture<Void> decryptContentFuture =
              algorithm.decryptAsync(tmpFile.toString(), finalFile.toString());

      try {
        while (!decryptContentFuture.isDone()) {
          sleep(100);
          progress.set((int) decryptContentFuture.getProgress());
        }
      } catch (InterruptedException e) {
        log.error("Encryption thread interrupted", e);
      }

      try {
        decryptContentFuture.get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("Encryption thread interrupted", e);
        return;
      }

      Files.deleteIfExists(tmpFile);

      if(fileService.isImage(finalFile.toString())) {
        writeMessage(chatFile, MessageType.INTERLOCUTOR_IMAGE, finalFile.toString(),
                finalFile, chatListView, curOpenChat, null);
      } else {
        writeMessage(chatFile, MessageType.INTERLOCUTOR_FILE, finalFile.toString(),
                finalFile, chatListView, curOpenChat, null);
      }

      filesProgress.remove(fileId);
    }
  }

  private void updateMessageFilePath(ListView<Message> chatListView, UUID fileId, Path finalFile) {
    for (Message msg : chatListView.getItems()) {
      if (msg.getFilePath() != null &&
              msg.getFilePath().getFileName().toString().equals(fileId.toString())) {
        msg.setFilePath(finalFile);
        break;
      }
    }
    chatListView.refresh();
  }

  public CompletableFuture<Void> interlocutorWriteFileMessageAsync(String username, String chatName, UUID fileId,
                                           byte[] fileName, byte[] fileContent, int chunkNumber, int totalChunks,
                                           ListView<Message> chatListView, Path curOpenChat,
                                           Map<String, SymmetricAlgorithm> encryptionAlgorithms) {

    return CompletableFuture.runAsync(() -> {
      try {
        interlocutorWriteFileMessage(username, chatName, fileId, fileName, fileContent,
                chunkNumber, totalChunks, chatListView, curOpenChat, encryptionAlgorithms);
      } catch (IOException e) {
        log.error("Interlocutor write file error {}", e.getMessage());
      }
    });
  }

  public void interlocutorParseAndWriteFileMessage(String username, String jsonMessage, ListView<Message> chatListView,
                                                   Path curOpenChat, Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {
    ChatFileMessage chatMessage = mapper.readValue(jsonMessage, ChatFileMessage.class);

    interlocutorWriteFileMessageAsync(
            username, chatMessage.getSender(), chatMessage.getFileId(), chatMessage.getFileName(),
            chatMessage.getFileContent(), chatMessage.getChunkNumber(), chatMessage.getTotalChunks(),
            chatListView, curOpenChat, encryptionAlgorithms);
  }

  private SymmetricAlgorithm getAlgorithm(String chatName, Path chatFile,
                                          Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {
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

    return algorithm;
  }

  private String addPrefix(MessageType type, String message) {
    return switch (type) {
      case MY_MESSAGE -> "@me@" + message;
      case INTERLOCUTOR_MESSAGE -> "@il@" + message;
      case MY_FILE -> "@mf@" + message;
      case INTERLOCUTOR_FILE -> "@if@" + message;
      case MY_IMAGE -> "@mi@" + message;
      case INTERLOCUTOR_IMAGE -> "@ii@" + message;
    };
  }
}
