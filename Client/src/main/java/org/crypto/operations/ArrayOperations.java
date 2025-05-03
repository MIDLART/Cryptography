package org.crypto.operations;

import java.nio.ByteBuffer;
import java.util.List;

import static org.crypto.operations.BitOperations.concatByteArrays;

public class ArrayOperations {
  public static byte[] listToArray(List<byte[]> list) {
    if (list == null || list.isEmpty()) return new byte[0];
    if (list.size() == 1) return list.getFirst();

    int blockSize = list.getFirst().length;
    int lastBlockSize = list.getLast().length;

    int size = (list.size() - 1) * blockSize + lastBlockSize;

    byte[] array = new byte[size];

    int i = 0;
    for (; i < list.size() - 1; i++) {
      System.arraycopy(
              list.get(i), 0,
              array, i * blockSize,
              blockSize);
    }

    if (lastBlockSize != 0) {
      System.arraycopy(
              list.getLast(), 0,
              array, i * blockSize,
              lastBlockSize);
    }

    return array;
  }

  public static long byteArrayToLongLittleEndian(byte[] bytes) {
    if (bytes.length > Long.BYTES) {
      throw new IllegalArgumentException("Array length must be <= " + Long.BYTES + " bytes");
    }

    long value = 0;
    for (int i = 0; i < bytes.length; i++) {
      value |= Byte.toUnsignedLong(bytes[i]) << (i * 8);
    }

    return value;
  }

  public static long byteArrayToLongBigEndian(byte[] bytes) {
    if (bytes.length > Long.BYTES) {
      throw new IllegalArgumentException("Array length must be <= " + Long.BYTES + " bytes");
    }

    long value = 0;
    for (int i = 0; i < bytes.length; i++) {
      value |= Byte.toUnsignedLong(bytes[i]) << ((bytes.length - 1 - i) * 8);
    }

    return value;
  }

  public static byte[] concatLongToByteArray(long L, long R) {
    byte[] arrL = ByteBuffer.allocate(Long.BYTES).putLong(L).array();
    byte[] arrR = ByteBuffer.allocate(Long.BYTES).putLong(R).array();

    return concatByteArrays(arrL, arrR);
  }
}
