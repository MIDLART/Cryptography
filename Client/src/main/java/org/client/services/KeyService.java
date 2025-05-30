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

    byte[] iv = Base64.getDecoder().decode((String) config.get("iv"));

    return new ChatSettings(
            chatName,
            Algorithm.valueOf((String) config.get("algorithm")),
            EncryptionMode.valueOf((String) config.get("encryptionMode")),
            PackingMode.valueOf((String) config.get("packingMode")),
            iv
    );
  }

  public ChatSettings readChatSettings(String username, String chatName) throws IOException {
    return readChatSettings(getConfigFilePath(username, chatName), chatName);
  }

  public ChatSettings settingsWithNewKey(String username, String chatName, byte[] newKey) {
    Path file;
    Map<String, Object> config;
    try {
      file = getConfigFilePath(username, chatName);
      config = mapper.readValue(file.toFile(), Map.class);
    } catch (IOException e) {
      log.error("Error reading config file", e);
      return null;
    }

    return new ChatSettings(
            chatName,
            Algorithm.valueOf((String) config.get("algorithm")),
            EncryptionMode.valueOf((String) config.get("encryptionMode")),
            PackingMode.valueOf((String) config.get("packingMode")),
            newKey
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
    String base64IV = Base64.getEncoder().encodeToString(settings.getIV());

    configData.put("key", base64Key);
    configData.put("algorithm", settings.getAlgorithm().name());
    configData.put("encryptionMode", settings.getEncryptionMode().name());
    configData.put("packingMode", settings.getPackingMode().name());
    configData.put("iv", base64IV);

    try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
      mapper.writeValue(out, configData);
    }

    return file;
  }

  public void writeInvitation(String username, String sender, BigInteger key, ChatSettings settings) throws IOException {
    Path file = getInvitationFilePath(username, sender);

    Map<String, Object> configData = new LinkedHashMap<>();

    String base64IV = Base64.getEncoder().encodeToString(settings.getIV());

    configData.put("key", key.toString());
    configData.put("algorithm", settings.getAlgorithm().name());
    configData.put("encryptionMode", settings.getEncryptionMode().name());
    configData.put("packingMode", settings.getPackingMode().name());
    configData.put("iv", base64IV);

    try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
      mapper.writeValue(out, configData);
    }
  }

  public Path updateKeyInConfig(String username, String chatName, byte[] newKey) throws IOException {
    Path configFile = getConfigFilePath(username, chatName);

    if (!Files.exists(configFile)) {
      return null;
    }

    Map<String, Object> configData = mapper.readValue(configFile.toFile(), Map.class);

    String base64NewKey = null;
    if (newKey != null) {
      base64NewKey = Base64.getEncoder().encodeToString(Arrays.copyOfRange(newKey, 0, 32));
    }

    configData.put("key", base64NewKey);

    try (OutputStream out = Files.newOutputStream(configFile,
            StandardOpenOption.TRUNCATE_EXISTING,
            StandardOpenOption.WRITE)) {
      mapper.writeValue(out, configData);
    }

    return configFile;
  }

  public boolean isKeyNull(String username, String chatName) {
    Path configFile;
    try {
      configFile = getConfigFilePath(username, chatName);
    } catch (IOException e) {
      log.error("Error reading config file", e);
      return true;
    }

    if (!Files.exists(configFile)) {
      log.error("config file does not exist");
    }

    Map<String, Object> configData = null;
    try {
      configData = mapper.readValue(configFile.toFile(), Map.class);
    } catch (IOException e) {
      log.error("Error mapping config file", e);
      return true;
    }
    return configData.get("key") == null;
  }
}
