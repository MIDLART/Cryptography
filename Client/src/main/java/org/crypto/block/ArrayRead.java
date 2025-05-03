package org.crypto.block;

import java.util.function.Function;

public class ArrayRead implements ReadBlock {
  private final byte[] input;
  private final int blockSize;
  private final long inputSize;
  private Function<byte[], byte[]> packingFunction = null;

  public ArrayRead(byte[] input, int blockSize, Function<byte[], byte[]> packing) {
    this.input = input;
    this.blockSize = blockSize;
    this.packingFunction = packing;

    inputSize = input.length;
  }

  public ArrayRead(byte[] input, int blockSize) {
    this.input = input;
    this.blockSize = blockSize;

    inputSize = input.length;
  }

  @Override
  public byte[] get(int index) {
    byte[] block;
    index *= blockSize;

    if (packingFunction != null && index + blockSize > inputSize) {
      int size = (int) (inputSize - index);

      block = new byte[size];
      System.arraycopy(input, index, block, 0, size);

      return packingFunction.apply(block);
    } else {
      block = new byte[blockSize];
      System.arraycopy(input, index, block, 0, blockSize);

      return block;
    }
  }
}
