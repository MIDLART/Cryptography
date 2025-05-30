package org.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.client.crypto.enums.EncryptionMode;
import org.client.crypto.enums.PackingMode;
import org.client.enums.Algorithm;
import org.client.models.ChatSettings;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.client.services.ChatService.getChatDirectoryPath;

@Getter
@Slf4j
@Service
public class KeyService {
  private final ObjectMapper mapper = new ObjectMapper();

  public Path getPrivateKeyFilePath(String username, String chatName) throws IOException {
    Path userChatDir = getChatDirectoryPath(username);

    return userChatDir.resolve(chatName + "_invitation.txt");
  }

  public static Path getConfigFilePath(String username, String chatName) throws IOException {
    Path userChatDir = getChatDirectoryPath(username);

    return userChatDir.resolve(chatName + "_conf.json");
  }

  public static Path getInvitationFilePath(String username, String chatName) throws IOException {
    Path userChatDir = getChatDirectoryPath(username);

    return userChatDir.resolve(chatName + "_deferred.json");
  }

  public BigInteger readInvitationPrivateKey(Path file) throws IOException {
    Map<String, Object> configData = mapper.readValue(file.toFile(), Map.class);
    String keyStr = (String) configData.get("key");

    return new BigInteger(keyStr);
  }

  public ChatSettings readChatSettings(Path file, String chatName) throws IOException {
    Map<String, Object> config = mapper.readValue(file.toFile(), Map.class);

    return new ChatSettings(
            chatName,
            Algorithm.valueOf((String) config.get("algorithm")),
            EncryptionMode.valueOf((String) config.get("encryptionMode")),
            PackingMode.valueOf((String) config.get("packingMode"))
    );
  }

  public BigInteger readPrivateKey(Path file) throws IOException {
    String keyString = Files.readString(file);

    return new BigInteger(keyString);
  }

  public void writePrivateKey(String username, String chatName, BigInteger key) throws IOException {
    Path file = getPrivateKeyFilePath(username, chatName);

    Files.writeString(
            file,
            key.toString(),
            StandardOpenOption.CREATE);
  }

  public Path writeConfig(String username, String chatName, byte[] key, ChatSettings settings) throws IOException {
    Path file = getConfigFilePath(username, chatName);

    Map<String, Object> configData = new LinkedHashMap<>();
    String base64Key = Base64.getEncoder().encodeToString(Arrays.copyOfRange(key, 0, 32));
    configData.put("key", base64Key);
    configData.put("algorithm", settings.getAlgorithm().name());
    configData.put("encryptionMode", settings.getEncryptionMode().name());
    configData.put("packingMode", settings.getPackingMode().name());

    try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
      mapper.writeValue(out, configData);
    }

    return file;
  }

  public void writeInvitation(String username, String sender, BigInteger key, ChatSettings settings) throws IOException {
    Path file = getInvitationFilePath(username, sender);

    Map<String, Object> configData = new LinkedHashMap<>();

    configData.put("key", key.toString());
    configData.put("algorithm", settings.getAlgorithm().name());
    configData.put("encryptionMode", settings.getEncryptionMode().name());
    configData.put("packingMode", settings.getPackingMode().name());

    try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
      mapper.writeValue(out, configData);
    }
  }
}
