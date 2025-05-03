package org.crypto.block;

import java.util.function.Function;

public class ArrayWrite implements WriteBlock {
  private final byte[] output;
  private final int blockSize;
  private Function<byte[], byte[]> unpackingFunction = null;

  public ArrayWrite(byte[] output, int blockSize) {
    this.output = output;
    this.blockSize = blockSize;
  }

  public ArrayWrite(byte[] output, int blockSize, Function<byte[], byte[]> unpackingFunction) {
    this.output = output;
    this.blockSize = blockSize;
    this.unpackingFunction = unpackingFunction;
  }

  @Override
  public void put(int index, byte[] block) {
    index *= blockSize;

    if (unpackingFunction != null && index + blockSize >= output.length) {
      System.arraycopy(unpackingFunction.apply(block), 0, output, index, block.length);
    } else {
      System.arraycopy(block, 0, output, index, blockSize);
    }
  }
}
