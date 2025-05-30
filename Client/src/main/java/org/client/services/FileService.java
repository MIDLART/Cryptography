package org.client.services;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.UUID;

@Log4j2
@Service
public class FileService {
  public static final String CHAT_DIR = "Client/src/main/resources/org/client/chats/";
  private static final int CHUNK_SIZE = 64 * 1024;

  public File attachFile(Label fileLabel) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Выберите файл");

    File file = fileChooser.showOpenDialog(fileLabel.getScene().getWindow());

    if (file != null) {
      Platform.runLater(() -> fileLabel.setText(file.getName()));
    }

    return file;
  }

  public Path getFileDirectoryPath(String username, String recipient) throws IOException {
    Path userFileDir = Paths.get(CHAT_DIR, username, recipient + "_files");

    if (!Files.exists(userFileDir)) {
      Files.createDirectories(userFileDir);
    }

    return userFileDir;
  }

  public Path saveFile(String username, String recipient, File file) {
    Path userFileDir;
    try {
      userFileDir = getFileDirectoryPath(username, recipient);
    } catch (IOException e) {
      log.error("Directory creation error", e);
      return null;
    }

    String originalFileName = file.getName();
    Path destinationPath = userFileDir.resolve(originalFileName);

    int counter = 1;
    while (Files.exists(destinationPath)) {
      String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
      String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
      String newFileName = baseName + "_" + counter + extension;
      destinationPath = userFileDir.resolve(newFileName);
      counter++;
    }

    try {
      Files.copy(file.toPath(), destinationPath);
      log.info("File successfully saved to: {}", destinationPath);
      return destinationPath;
    } catch (IOException e) {
      log.error("Failed to save file", e);
      return null;
    }
  }

  public synchronized Path chunkRec(String username, String sender, byte[] chunk, int chunkNumber, UUID fileId) {
    Path userFileDir;
    try {
      userFileDir = getFileDirectoryPath(username, sender);
    } catch (IOException e) {
      log.error("Directory creation error", e);
      return null;
    }

    Path filePath = userFileDir.resolve(fileId.toString());

    try (FileChannel channel = FileChannel.open(
            filePath,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.READ)) {

      long offset = (long) (chunkNumber - 1) * CHUNK_SIZE;
      channel.position(offset);
      ByteBuffer buffer = ByteBuffer.wrap(chunk);

      while (buffer.hasRemaining()) {
        channel.write(buffer);
      }

      channel.force(true);

      log.debug("Saved chunk {} for file {} at offset {} (size: {} bytes)",
              chunkNumber, fileId.toString(), offset, chunk.length);

    } catch (IOException e) {
      log.error("Error saving chunk {} for file {} at offset {}",
              chunkNumber, fileId.toString(), (long) (chunkNumber - 1) * CHUNK_SIZE, e);
      return null;
    }

    return filePath;
  }

  public Path createFile(String username, String recipient, String fileName) {
    Path userFileDir;
    try {
      userFileDir = getFileDirectoryPath(username, recipient);
    } catch (IOException e) {
      log.error("Directory creation error", e);
      return null;
    }

    Path filePath = userFileDir.resolve(fileName);

    int counter = 1;
    while (Files.exists(filePath)) {
      String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
      String extension = fileName.substring(fileName.lastIndexOf('.'));
      String newFileName = baseName + "_" + counter + extension;
      filePath = userFileDir.resolve(newFileName);
      counter++;
    }

    try {
      Files.createFile(filePath);
    } catch (IOException e) {
      log.error("Failed to create file", e);
    }

    return filePath;
  }

  public void removeLine(Path filePath, String targetLine) {
    try (RandomAccessFile file = new RandomAccessFile(filePath.toFile(), "rw")) {
      String currentLine;
      long startPos = 0;

      while ((currentLine = file.readLine()) != null) {
        if (currentLine.equals(targetLine)) {
          file.seek(startPos);
          file.writeBytes("");
          break;
        }

        startPos = file.getFilePointer();
      }
    } catch (IOException e) {
      log.error("Failed to remove line", e);
    }
  }

  public Path renameFile(Path filePath, String newName) throws IOException {
    Path parentDir = filePath.getParent();
    Path newFilePath = parentDir.resolve(newName);

    if (Files.exists(newFilePath)) {
      int counter = 1;
      while (Files.exists(newFilePath)) {
        String baseName = newName.substring(0, newName.lastIndexOf('.'));
        String extension = newName.substring(newName.lastIndexOf('.'));
        String newFileName = baseName + "_" + counter + extension;
        newFilePath = parentDir.resolve(newFileName);
        counter++;
      }
    }

    try {
      return Files.move(
              filePath,
              newFilePath,
              StandardCopyOption.ATOMIC_MOVE,
              StandardCopyOption.REPLACE_EXISTING
      );
    } catch (IOException e) {
      throw new IOException("Не удалось переименовать файл: " + e.getMessage(), e);
    }
  }

  public boolean isImage(String fileName) {
    String extension = fileName.substring(fileName.lastIndexOf('.')).toLowerCase();

    return extension.equals(".jpg") || extension.equals(".jpeg") ||
           extension.equals(".png") || extension.equals(".gif") ||
           extension.equals(".bmp") || extension.equals(".webp");
  }
}
