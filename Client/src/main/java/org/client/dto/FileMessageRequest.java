package org.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMessageRequest {
  private String sender;
  private String recipient;
  private UUID fileId;
  private byte[] fileName;
  private byte[] fileContent;
  private int chunkNumber;
  private int totalChunks;
}
