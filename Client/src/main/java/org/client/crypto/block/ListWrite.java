package org.client.crypto.block;

import java.util.List;
import java.util.function.Function;

public class ListWrite implements WriteBlock {
  private final List<byte[]> output;
  private final int blockSize;
  private final Function<byte[], byte[]> unpackingFunction;
  private final int arrSize;

  public ListWrite(List<byte[]> output, int blockSize, int arrSize, Function<byte[], byte[]> unpackingFunction) {
    this.output = output;
    this.blockSize = blockSize;
    this.unpackingFunction = unpackingFunction;
    this.arrSize = arrSize;
  }

  @Override
  public void put(int index, byte[] block) {
    if (index * blockSize + blockSize >= arrSize) {
      output.set(index, unpackingFunction.apply(block));
    } else {
      output.set(index, block);
    }
  }
}
