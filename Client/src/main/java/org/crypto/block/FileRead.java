package org.crypto.block;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;

public class FileRead implements ReadBlock {
  private final String fileName;
  private final int blockSize;
  private final long fileSize;
  private Function<byte[], byte[]> packingFunction = null;

  public FileRead(String fileName, int blockSize, Function<byte[], byte[]> packing) {
    this.fileName = fileName;
    this.blockSize = blockSize;

    fileSize = new File(fileName).length();
    this.packingFunction = packing;
  }

  public FileRead(String fileName, int blockSize) {
    this.fileName = fileName;
    this.blockSize = blockSize;

    fileSize = new File(fileName).length();
  }

  @Override
  public byte[] get(int index) {
    byte[] block = new byte[blockSize];
    index *= blockSize;

    try (FileChannel input = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
      input.position(index);

      if (packingFunction != null && index + blockSize > fileSize) {
        byte[] buf = new byte[(int) (fileSize - index)];
        input.read(ByteBuffer.wrap(buf));
        System.arraycopy(packingFunction.apply(buf), 0, block, 0, blockSize);
      } else {
        input.read(ByteBuffer.wrap(block));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return block;
  }
}
