package org.crypto.operations;

public class BitOperations {
  public static byte bitAt(int bitIndex, byte value) {
    return (byte) ((Byte.toUnsignedInt(value) >>> (7 - bitIndex % 8)) & 1);
  }

  public static byte bitAt(int bitIndex, final byte[] value) {
    return (byte) ((Byte.toUnsignedInt(value[bitIndex / 8]) >>> (7 - bitIndex % 8)) & 1);
  }

  public static byte zeroMaskLow(int bitCount) {
    return (byte) (~((1 << bitCount) - 1));
  }

  public static byte zeroMaskHigh(int bitCount) {
    return (byte) ((1 << (8 - bitCount)) - 1);
  }

  public static byte[] concatByteArrays(byte[] arr1, byte[] arr2) {
    byte[] res = new byte[arr1.length + arr2.length];

    System.arraycopy(arr1, 0, res, 0, arr1.length);
    System.arraycopy(arr2, 0, res, arr1.length, arr2.length);

    return res;
  }

  public static byte[] xor(byte[] arr1, byte[] arr2) {
    int len = arr1.length;
    byte[] res = new byte[len];

    for (int i = 0; i < len; i++) {
      res[i] = (byte) (arr1[i] ^ arr2[i]);
    }

    return res;
  }

  public static long zeroMask(int bitCount) {
    if (bitCount == 64) {
      return -1;
    }

    return (1L << bitCount) - 1;
  }

  public static long leftRotation(long x, long y, int size) {
    y %= size;

    if (size == 64) {
      return x << y | (x >>> (size - y));
    }

    return (x << y | (x >>> (size - y))) & zeroMask(size);
  }

  public static long rightRotation(long x, long y, int size) {
    y %= size;

    if (size == 64) {
      return x >>> y | (x << (size - y));
    }

    return (x >>> y | (x << (size - y))) & zeroMask(size);
  }

  public static long getBits(long x, int left, int right) {
    int leftShift = 63 - left;

    return x << (leftShift) >>> (right + leftShift);
  }
}
