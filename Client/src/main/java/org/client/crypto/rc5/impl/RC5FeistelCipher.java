package org.client.crypto.rc5.impl;

import java.util.Arrays;
import java.util.List;

import static org.client.crypto.operations.ArrayOperations.byteArrayToLongLittleEndian;
import static org.client.crypto.operations.BitOperations.*;
import static org.client.crypto.operations.MathOperations.*;

public class RC5FeistelCipher {
  private final int w; // половина длинны блока(бит)
  private final int r; // число раундов

  private final int u; // половина длинны блока(байт)

  private final List<Long> S;

  public RC5FeistelCipher(int w, int r, byte[] inputKey) {
    if (inputKey.length > 255) {
      throw new IllegalArgumentException("Invalid key length: " + inputKey.length);
    }

    if (w != 16 && w != 32 && w != 64) {
      throw new IllegalArgumentException("Invalid block size: " + w * 2 + " bit");
    }

    if (r < 1 || r > 255) {
      throw new IllegalArgumentException("Invalid rounds count: " + r);
    }

    this.w = w;
    this.r = r;

    this.u = w / 8;

    S = (new RC5GenKey(w, r, inputKey.length)).genKey(inputKey);
  }

  public byte[] encrypt(byte[] text) {
    long A = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, 0, u));
    long B = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, u, text.length));

    A = moduloAdd(A, S.get(0), w);
    B = moduloAdd(B, S.get(1), w);

    for (int i = 1; i <= r; i++) {
      A = moduloAdd(leftRotation(A ^ B, B, w), S.get(2 * i),     w);
      B = moduloAdd(leftRotation(B ^ A, A, w), S.get(2 * i + 1), w);
    }

    return concatAB(A, B);
  }

  public byte[] decrypt(byte[] text) {
    long A = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, 0, u));
    long B = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, u, text.length));

    for (int i = r; i >= 1; i--) {
      B = rightRotation(moduloSub(B, S.get(2 * i + 1), w), A, w) ^ A;
      A = rightRotation(moduloSub(A, S.get(2 * i),     w), B, w) ^ B;
    }

    B = moduloSub(B, S.get(1), w);
    A = moduloSub(A, S.get(0), w);

    return concatAB(A, B);
  }

//  public byte[] encrypt(byte[] text) {
//    long A = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, 0, u));
//    long B = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, u, 2*u));
//
//    A = moduloAdd(A, S.get(0), w);
//    B = moduloAdd(B, S.get(1), w);
//
//    for (int i = 1; i <= r; i++) {
//      A = moduloAdd(leftRotation(A ^ B, B, w), S.get(2*i), w);
//      B = moduloAdd(leftRotation(B ^ A, A, w), S.get(2*i + 1), w);
//    }
//
//    return concatAB(A, B);
//  }
//
//  public byte[] decrypt(byte[] text) {
//    long A = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, 0, u));
//    long B = byteArrayToLongLittleEndian(Arrays.copyOfRange(text, u, 2*u));
//
//    for (int i = r; i >= 1; i--) {
//      B = rightRotation(moduloSub(B, S.get(2*i + 1), w), A, w) ^ A;
//      A = rightRotation(moduloSub(A, S.get(2*i), w), B, w) ^ B;
//    }
//
//    B = moduloSub(B, S.get(1), w);
//    A = moduloSub(A, S.get(0), w);
//
//    return concatAB(A, B);
//  }

  private byte[] concatAB(long A, long B) {
    byte[] res = new byte[u * 2];

    for (int i = 0; i < u; i++) {
      res[i] = (byte) ((A >>> (8 * i)) & 0xff);
      res[u + i] = (byte) ((B >>> (8 * i)) & 0xff);
    }

    return res;
  }
}
