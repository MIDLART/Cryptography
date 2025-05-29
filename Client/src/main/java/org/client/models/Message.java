package org.client.models;

import javafx.beans.property.IntegerProperty;
import lombok.Data;
import org.client.enums.MessageType;

import java.nio.file.Path;

import static org.client.enums.MessageType.*;

@Data
public class Message {
  private final String text;
  private final MessageType type;
  private Path filePath;
  private final IntegerProperty progress;

  public Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
    this.filePath = null;
    this.progress = null;
  }

  public Message(MessageType type, Path filePath, IntegerProperty progress) {
    this.text = null;
    this.type = type;
    this.filePath = filePath;
    this.progress = progress;
  }

  public Message(String text, MessageType type, Path filePath, IntegerProperty progress) {
    this.text = text;
    this.type = type;
    this.filePath = filePath;
    this.progress = progress;
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
}
