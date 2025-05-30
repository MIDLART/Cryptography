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
import org.client.crypto.loki97.LOKI97;
import org.client.crypto.rc5.RC5;
import org.client.dto.ChatFileMessage;
import org.client.dto.ChatMessage;
import org.client.enums.Algorithm;
import org.client.enums.MessageType;
import org.client.models.Message;
import org.springframework.stereotype.Service;

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
import static org.client.crypto.loki97.boxes.Sboxes.initS1;
import static org.client.crypto.loki97.boxes.Sboxes.initS2;
import static org.client.services.FileService.getFileDirectoryPath;
import static org.client.services.KeyService.getConfigFilePath;

@Getter
@Slf4j
@Service
public class ChatService {
  public static final String CHAT_DIR = "Client/src/main/resources/org/client/chats/";
  private final ObjectMapper mapper = new ObjectMapper();

  private final FileService fileService = new FileService();
  private final Map<UUID, Integer> filesProgress = new HashMap<>();

  private static final Object FILE_LOCK = new Object();

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

  public static Path createChatFilePath(String username, String chatName) throws IOException {
    Path chatFile = getChatFilePath(username, chatName);

    if (!Files.exists(chatFile)) {
      Files.createFile(chatFile);
    }

    return chatFile;
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

        getAlgorithm(username, chatName, encryptionAlgorithms);

        ObservableList<Message> messages = FXCollections.observableArrayList();

        for (int i = 0; i < lines.size(); i++) {
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
          chatListView.setCellFactory(lv -> new MessageCell(chatListView));
        });
      }
    } catch (IOException e) {
      log.error("Ошибка работы с файлом чата", e);
      throw new IOException();
    }

    return chatFile;
  }

  public class MessageCell extends ListCell<Message> {
    private final ListView<Message> chatListView;

    public MessageCell(ListView<Message> chatListView) {
      this.chatListView = chatListView;
    }
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

          viewImage(container, msg);

          if (msg.progressProperty() != null) {
            progressCheck(container, msg, chatListView);
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
          progressCheck(mainContainer, msg, chatListView);
        }

        setGraphic(mainContainer);
        setText(null);
      } else {
        setText(msg.getText());
        setGraphic(null);
      }
    }
  }

  private void viewImage(VBox container, Message msg) {
    Image image = new Image(msg.getFilePath().toUri().toString());
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(200);
    imageView.setPreserveRatio(true);
    container.getChildren().add(imageView);
  }

  private void progressCheck(VBox container, Message msg, ListView<Message> chatListView) {
    HBox progressContainer = new HBox(5);
    progressContainer.setAlignment(msg.isMe() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

    ProgressBar progressBar = new ProgressBar();
    progressBar.setPrefWidth(200);
    progressBar.setPrefHeight(5);
    progressBar.progressProperty().bind(
            msg.progressProperty().divide(100.0)
    );

    Button cancelButton = new Button("✕");
    cancelButton.getStyleClass().add("cancel-button");
    cancelButton.setOnAction(e -> cancelButtonAction(msg, chatListView));

    msg.progressProperty().addListener((obs, oldValue, newValue) -> {
      if (newValue.doubleValue() >= 100.0 && !msg.isReached100()) {
        msg.setReached100(true);
      }
    });

    cancelButton.visibleProperty().bind(
            msg.progressProperty().lessThan(100)
                    .and(msg.reached100Property().not())
    );

    progressBar.visibleProperty().bind(
            msg.progressProperty().lessThan(100)
    );

    progressContainer.getChildren().addAll(progressBar, cancelButton);
    container.getChildren().add(progressContainer);
  }

  private void cancelButtonAction(Message msg, ListView<Message> chatListView) {
    msg.cancel();

    fileService.removeLine(msg.getFilePath(), addPrefix(msg.getType(), msg.getFilePath().toString()));

    Path dir = msg.getFilePath().getParent();
    try {
      Files.delete(Paths.get(dir.toAbsolutePath().toString(), msg.getFileId().toString()));
      Files.delete(msg.getFilePath());
    } catch (IOException e) {
      log.error("delete file failed", e);
    }

    Platform.runLater(() -> {
      ObservableList<Message> items = chatListView.getItems();
      items.remove(msg);
    });
  }

  public void writeMessage(Path chatFile, MessageType type, String message, Path filePath,
                           ListView<Message> chatListView, Path curOpenChat, IntegerProperty progress,
                           CancellableCompletableFuture<Void> encryptFuture, UUID id) throws IOException {

    if (chatFile != null && Files.exists(chatFile)) {
      message += "\n";

      Message newMessage = new Message(message, type, filePath, progress, encryptFuture, id);

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
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
      }
    }
  }

  public void meWriteMessage(Path chatFile, String message, ListView<Message> chatListView, Path curOpenChat) throws IOException {
    writeMessage(chatFile, MessageType.MY_MESSAGE, message, null, chatListView,
            curOpenChat, null, null, null);
  }

  public void interlocutorWriteMessage(String username, String chatName, byte[] message,
                                       ListView<Message> chatListView, Path curOpenChat,
                                       Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {

    Path chatFile = getChatFilePath(username, chatName);

    SymmetricAlgorithm algorithm = getAlgorithm(username, chatName, encryptionAlgorithms);

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
            null, chatListView, curOpenChat, null, null, null);
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
                          Path filePath, IntegerProperty progress, CancellableCompletableFuture<Void> encryptFuture, UUID id) throws IOException {
    writeMessage(chatFile, MessageType.MY_FILE, filePath.toString(), filePath, chatListView, curOpenChat, progress, encryptFuture, id);
  }

  public void meWriteImage(Path chatFile, ListView<Message> chatListView, Path curOpenChat,
                           Path filePath, IntegerProperty progress, CancellableCompletableFuture<Void> encryptFuture, UUID id) throws IOException {
    writeMessage(chatFile, MessageType.MY_IMAGE, filePath.toString(), filePath, chatListView, curOpenChat, progress, encryptFuture, id);
  }

  public void interlocutorWriteFileMessage(String username, String chatName, UUID fileId,
                                           byte[] fileName, byte[] fileContent, int chunkNumber,
                                           int totalChunks, ListView<Message> chatListView,
                                           Path curOpenChat, Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {

    Path chatFile = getChatFilePath(username, chatName);

    SymmetricAlgorithm algorithm = getAlgorithm(username, chatName, encryptionAlgorithms);

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
                finalFile, chatListView, curOpenChat, null, null, null);
      } else {
        writeMessage(chatFile, MessageType.INTERLOCUTOR_FILE, finalFile.toString(),
                finalFile, chatListView, curOpenChat, null, null, null);
      }

      filesProgress.remove(fileId);
    }
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

  private SymmetricAlgorithm getAlgorithm(String username, String chatName,
                                          Map<String, SymmetricAlgorithm> encryptionAlgorithms) throws IOException {
    SymmetricAlgorithm algorithm;
    if(!encryptionAlgorithms.containsKey(chatName)) {
      Map<String, Object> data = mapper.readValue(getConfigFilePath(username, chatName).toFile(), Map.class);

      if (data.get("key") == null) return null;

      byte[] key = Base64.getDecoder().decode(data.get("key").toString());
      Algorithm encryptionAlgorithm = Algorithm.valueOf((String) data.get("algorithm"));
      EncryptionMode encryptionMode = EncryptionMode.valueOf((String) data.get("encryptionMode"));
      PackingMode packingMode = PackingMode.valueOf((String) data.get("packingMode"));
      byte[] initVector = Base64.getDecoder().decode(data.get("iv").toString());

      if (encryptionAlgorithm == Algorithm.RC5) {
        algorithm = new SymmetricAlgorithm(
                new RC5(key, 16), encryptionMode, packingMode, initVector);
      } else {
        algorithm = new SymmetricAlgorithm(
                new LOKI97(key, initS1(), initS2()), encryptionMode, packingMode, initVector);
      }

      encryptionAlgorithms.put(chatName, algorithm);
    } else {
      algorithm = encryptionAlgorithms.get(chatName);
    }

    return algorithm;
  }

  public void deleteChat(String username, String recipient) {
    try {
      Path chatFile = getChatFilePath(username, recipient);
      Path configFile = getConfigFilePath(username, recipient);
      Path filesDir = getFileDirectoryPath(username, recipient);

      if (Files.exists(chatFile)) {
        Files.delete(chatFile);
      }

      if (Files.exists(configFile)) {
        Files.delete(configFile);
      }

      if (Files.exists(filesDir)) {
        Files.walk(filesDir)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                  try {
                    Files.delete(path);
                    log.debug("Deleted file: {}", path);
                  } catch (IOException e) {
                    log.error("Failed to delete file: {}", path, e);
                  }
                });
      }

    } catch (IOException e) {
      log.error("Delete chat error for {}: {}", recipient, e.getMessage());
    }
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
