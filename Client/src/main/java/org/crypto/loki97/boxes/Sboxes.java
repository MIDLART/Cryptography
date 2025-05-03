package org.crypto.loki97.boxes;

import static org.crypto.operations.GaloisFieldOperations.exp3;

public class Sboxes {
  private static final int S1Irreducible = 0x2911;
  private static final int S2Irreducible = 0xAA7;

  private static final int S1Size = 8192;
  private static final int S2Size = 2048;

  public static byte[] initS1() {
    byte[] S1 = new byte[S1Size];

    for (int i = 0; i < S1Size; i++) {
      S1[i] = exp3(i ^ 0x1FFF, S1Irreducible, S1Size);
    }

    return S1;
  }

  public static byte[] initS2() {
    byte[] S2 = new byte[S2Size];

    for (int i = 0; i < S2Size; i++) {
      S2[i] = exp3(i ^ 0x7FF, S2Irreducible, S2Size);
    }

    return S2;
  }
}
