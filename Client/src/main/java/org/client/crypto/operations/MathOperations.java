package org.client.crypto.operations;

import static org.client.crypto.operations.BitOperations.zeroMask;

public class MathOperations {
  public static long moduloAdd(long a, long b, int modulePow) {
    if (modulePow == 64) {
      return a + b;
    }

    return (a + b) & zeroMask(modulePow);
  }

  public static long moduloSub(long a, long b, int modulePow) {
    if (modulePow == 64) {
      return a - b;
    }

    return (a - b) & zeroMask(modulePow);
  }

  public static long moduloAdd(long a, long b, long c, int modulePow) {
    if (modulePow == 64) {
      return a + b + c;
    }

    return (a + b + c) & zeroMask(modulePow);
  }
}
