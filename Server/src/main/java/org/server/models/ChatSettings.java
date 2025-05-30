package org.server.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.server.enums.Algorithm;
import org.server.enums.EncryptionMode;
import org.server.enums.PackingMode;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSettings {
  private String recipient;

  private Algorithm algorithm;
  private EncryptionMode encryptionMode;
  private PackingMode packingMode;
  private byte[] IV;
}
