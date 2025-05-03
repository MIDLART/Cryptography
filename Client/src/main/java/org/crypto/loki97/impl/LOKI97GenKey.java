package org.crypto.loki97.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.crypto.operations.ArrayOperations.byteArrayToLongBigEndian;

public class LOKI97GenKey {
  private final int keyLength;

  private final LOKI97FeistelFunction f;
  private final long delta = 0x9E3779B97F4A7C15L;

  public LOKI97GenKey(int keyLength, LOKI97FeistelFunction f) {
    if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
      throw new IllegalArgumentException("Invalid key length: " + keyLength);
    }

    this.f = f;
    this.keyLength = keyLength;
  }

  public List<Long> genKey(byte[] inputKey) {
    long[] K = getK(inputKey);
    List<Long> SK = new ArrayList<>();

    long d = delta;

    for (int i = 1; i <= 48; i++) {
      SK.add(K[3] ^ gFunction(K, d));

      K[3] = K[2];
      K[2] = K[1];
      K[1] = K[0];
      K[0] = SK.getLast();

      d += delta;
    }

    return SK;
  }

  private long gFunction(long[] K, long d) {
    return f.encrypt(K[0] + K[2] + d, K[1]);
  }

  private long[] getK(byte[] inputKey) {
    long[] K = new long[4];

    K[3] = (byteArrayToLongBigEndian(Arrays.copyOfRange(inputKey, 0, 8)));
    K[2] = (byteArrayToLongBigEndian(Arrays.copyOfRange(inputKey, 8, 16)));

    switch (keyLength) {
      case 32 -> {
        K[1] = (byteArrayToLongBigEndian(Arrays.copyOfRange(inputKey, 16, 24)));
        K[0] = (byteArrayToLongBigEndian(Arrays.copyOfRange(inputKey, 24, 32)));
      }
      case 24 -> {
        K[1] = (byteArrayToLongBigEndian(Arrays.copyOfRange(inputKey, 16, 24)));
        K[0] = (f.encrypt(K[0], K[1]));
      }
      case 16 -> {
        K[1] = (f.encrypt(K[1], K[0]));
        K[0] = (f.encrypt(K[0], K[1]));
      }
    }

    return K;
  }
}
