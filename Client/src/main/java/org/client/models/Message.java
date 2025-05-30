package org.client.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import lombok.Data;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.enums.MessageType;

import java.nio.file.Path;
import java.util.UUID;

import static org.client.enums.MessageType.*;

@Data
public class Message {
  private final String text;
  private final MessageType type;
  private Path filePath;
  private final IntegerProperty progress;
  private final CancellableCompletableFuture<Void> encryptFuture;
  private final BooleanProperty reached100 = new SimpleBooleanProperty(false);
  private UUID fileId = null;

  public Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
    this.filePath = null;
    this.progress = null;
    this.encryptFuture = null;
  }

  public Message(MessageType type, Path filePath, IntegerProperty progress) {
    this.text = null;
    this.type = type;
    this.filePath = filePath;
    this.progress = progress;
    this.encryptFuture = null;
  }

  public Message(String text, MessageType type, Path filePath, IntegerProperty progress,
                 CancellableCompletableFuture<Void> encryptFuture, UUID fileId) {
    this.text = text;
    this.type = type;
    this.filePath = filePath;
    this.progress = progress;
    this.encryptFuture = encryptFuture;
    this.fileId = fileId;
  }

  public boolean isMe() {
    return type == MY_MESSAGE || type == MY_FILE || type == MY_IMAGE;
  }

  public boolean isImage() {
    return filePath != null && type != null &&
            (type == MessageType.MY_IMAGE || type == MessageType.INTERLOCUTOR_IMAGE);
  }

  public boolean isFile() {
    return filePath != null && type != null &&
            (type == MessageType.MY_FILE || type == MessageType.INTERLOCUTOR_FILE);
  }

  public IntegerProperty progressProperty() {
    return progress;
  }

  public void setProgress(int progress) {
    this.progress.set(progress);
  }

  public int getProgress() {
    return progress.get();
  }

  public BooleanProperty reached100Property() {
    return reached100;
  }

  public boolean isReached100() {
    return reached100.get();
  }

  public void setReached100(boolean value) {
    reached100.set(value);
  }

  public void cancel() {
    reached100.set(true);
    encryptFuture.cancel(true);
  }
}
