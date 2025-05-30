package org.client.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.enums.Algorithm;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSettings {
  private String recipient;

  private Algorithm algorithm;
  private EncryptionMode encryptionMode;
  private PackingMode packingMode;
}
