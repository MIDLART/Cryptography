package org.client.models;

import lombok.Data;
import org.client.enums.MessageType;

import java.nio.file.Path;

import static org.client.enums.MessageType.*;

@Data
public class Message {
  private final String text;
  private final MessageType type;
  private final Path filePath;

  public Message(String text, MessageType type) {
    this.text = text;
    this.type = type;
    this.filePath = null;
  }

  public Message(MessageType type, Path filePath) {
    this.text = null;
    this.type = type;
    this.filePath = filePath;
  }

  public Message(String text, MessageType type, Path filePath) {
    this.text = text;
    this.type = type;
    this.filePath = filePath;
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
}
