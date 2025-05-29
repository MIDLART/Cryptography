package org.server.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatFileMessage {
  private String sender;
  private UUID fileId;
  private byte[] fileName;
  private byte[] fileContent;
  private int chunkNumber;
  private int totalChunks;
}
