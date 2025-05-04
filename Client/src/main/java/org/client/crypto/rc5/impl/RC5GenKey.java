package org.client.crypto.rc5.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.client.crypto.operations.BitOperations.*;
import static org.client.crypto.operations.MathOperations.moduloAdd;

public class RC5GenKey {
  private final int w; // половина длинны блока(бит)
  private final int r; // число раундов
  private final int b; // длина ключа(байт)

  // Магические константы
  private final long P16 = 0xb7e1L;
  private final long Q16 = 0x9e37L;

  private final long P32 = 0xb7e15163L;
  private final long Q32 = 0x9e3779b9L;

  private final long P64 = 0xb7e151628aed2a6bL;
  private final long Q64 = 0x9e3779b97f4a7c15L;

  private final List<Long> P = List.of(P16, P32, P64);
  private final List<Long> Q = List.of(Q16, Q32, Q64);

  public RC5GenKey(int w, int r, int b) {
    this.b = b;
    this.w = w;
    this.r = r;
  }

//  @Override
//  public List<byte[]> genKey(byte[] inputKey) {
//    int u = w / 8;
//    int c = Math.max(1, b / (u));
//    int t = 2 * (r + 1);
//
//    List<Long> L = splitKey(inputKey, u, c);
//    List<Long> S = extendedKeys(t);
//    mixing(L, S, c, t);
//
//    return listLongToByteArrayList(S);
//  }

  public List<Long> genKey(byte[] inputKey) {
    int u = w / 8;
    int c = (int) Math.ceil((double) Math.max(1, b) / u);
    int t = 2 * (r + 1);

    List<Long> L = splitKey(inputKey, u, c);
    List<Long> S = extendedKeys(t);
    mixing(L, S, c, t);

    return S;
  }

  private List<Long> splitKey(byte[] inputKey, int u, int c) {
    if (b == 0) {
      return new ArrayList<>(List.of(0L));
    }

    List<Long> L = new ArrayList<>(Collections.nCopies(c, 0L));

    for (int i = b - 1; i >= 0; i--) {
      L.set(i / u, moduloAdd(
                      leftRotation(L.get(i / u), 8, w),
                      Byte.toUnsignedInt(inputKey[i]), w));
    }

    return L;
  }

  private List<Long> extendedKeys(int t) {
    int index = switch (w) { // Индекс магических констант соответствующей размерности
                case 16 -> 0;
                case 32 -> 1;
                case 64 -> 2;
                default -> throw new IllegalStateException("Unexpected value: " + w);
    };

    List<Long> S = new ArrayList<>();

    S.add(P.get(index));
    for(int i = 0; i < t - 1; i++) {
      S.add(moduloAdd(S.get(i), Q.get(index), w));
    }

    return S;
  }

  private void mixing(List<Long> L, List<Long> S, int c, int t) {
    int iterations = 3 * Math.max(c, t);

    int i = 0, j = 0;
    long A = 0, B = 0;
    for (int k = 0; k < iterations; k++) {
      A = leftRotation(moduloAdd(S.get(i), A, B, w), 3, w);
      B = leftRotation(moduloAdd(L.get(j), A, B, w), moduloAdd(A, B, w), w);

      S.set(i, A);
      L.set(j, B);

      i = (i + 1) % t;
      j = (j + 1) % c;
    }
  }
}
