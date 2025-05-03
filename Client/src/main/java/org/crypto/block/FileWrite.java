package org.crypto.block;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class FileWrite implements WriteBlock {
  private final String fileName;
  private final int blockSize;
  private Function<byte[], byte[]> unpackingFunction = null;
  private long fileSize = 0;

  public FileWrite(String fileName, int blockSize) {
    this.fileName = fileName;
    this.blockSize = blockSize;
  }

  public FileWrite(String fileName, int blockSize, long fileSize, Function<byte[], byte[]> unpackingFunction) {
    this.fileName = fileName;
    this.blockSize = blockSize;
    this.unpackingFunction = unpackingFunction;
    this.fileSize = fileSize;
  }

  @Override
  public void put(int index, byte[] block) {
    index *= blockSize;

    try (FileChannel output = FileChannel.open(
            Paths.get(fileName),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)) {

      output.position(index);

      if (unpackingFunction != null && index + blockSize >= fileSize) {
        output.write(ByteBuffer.wrap(unpackingFunction.apply(block)));
      } else {
        output.write(ByteBuffer.wrap(block));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
