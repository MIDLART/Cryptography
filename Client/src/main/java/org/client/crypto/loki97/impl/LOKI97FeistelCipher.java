package org.client.crypto.loki97.impl;

import java.util.Arrays;
import java.util.List;

import static org.client.crypto.operations.ArrayOperations.byteArrayToLongBigEndian;
import static org.client.crypto.operations.ArrayOperations.concatLongToByteArray;

public class LOKI97FeistelCipher {
  private final LOKI97FeistelFunction f;
  private final List<Long> SK;

  public LOKI97FeistelCipher(byte[] inputKey, byte[] s1, byte[] s2) {
    f = new LOKI97FeistelFunction(s1, s2);
    SK = (new LOKI97GenKey(inputKey.length, f)).genKey(inputKey);
  }

  public byte[] encrypt(byte[] text) {
    long L = byteArrayToLongBigEndian(Arrays.copyOfRange(text, 0, 8));
    long R = byteArrayToLongBigEndian(Arrays.copyOfRange(text, 8, 16));

    long prevR;

    for (int i = 1; i <= 16; i++) {
      prevR = R;

      R = L ^ f.encrypt(R + SK.get(3 * i - 2 - 1), SK.get(3 * i - 1 - 1));
      L = prevR + SK.get(3 * i - 2 - 1) + SK.get(3 * i - 1);
    }

    return concatLongToByteArray(R, L);
  }

  public byte[] decrypt(byte[] text) {
    long R = byteArrayToLongBigEndian(Arrays.copyOfRange(text, 0, 8));
    long L = byteArrayToLongBigEndian(Arrays.copyOfRange(text, 8, 16));

    long prevL;

    for (int i = 16; i >= 1; i--) {
      prevL = L;

      L = R ^ f.encrypt(L - SK.get(3 * i - 1), SK.get(3 * i - 1 - 1));
      R = prevL - SK.get(3 * i - 1) - SK.get(3 * i - 2 - 1);
    }

    return concatLongToByteArray(L, R);
  }
}
