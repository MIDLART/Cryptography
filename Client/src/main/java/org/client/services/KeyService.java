package org.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.client.services.ChatService.getChatDirectoryPath;
import static org.client.services.ChatService.getChatFilePath;

@Getter
@Slf4j
@Service
public class KeyService {
  private final ObjectMapper mapper = new ObjectMapper();

  public Path getPrivateKeyFilePath(String username, String chatName) throws IOException {
    Path userChatDir = getChatDirectoryPath(username);

    return userChatDir.resolve(chatName + "_invitation.txt");
  }

  public BigInteger readPrivateKey(Path chatFile) throws IOException {
    String keyString = Files.readString(chatFile);

    return new BigInteger(keyString);
  }

  public void writePrivateKey(String username, String chatName, BigInteger key) throws IOException {
    Path chatFile = getPrivateKeyFilePath(username, chatName);

    Files.writeString(
            chatFile,
            key.toString(),
            StandardOpenOption.CREATE);
  }

  public Path writeFinalKey(String username, String chatName, byte[] key) throws IOException {
    Path chatFile = getChatFilePath(username, chatName);

    Files.writeString(
            chatFile,
            Arrays.toString(key),
            StandardOpenOption.CREATE);

    Files.writeString(chatFile, "\n", StandardOpenOption.APPEND);

    return chatFile;
  }
}
