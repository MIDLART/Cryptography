package org.client.models;

import lombok.Data;

@Data
public class Message {
  private final String text;
  private final boolean isMe;
}
