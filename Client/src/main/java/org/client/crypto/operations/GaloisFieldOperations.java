package org.client.crypto.operations;

public class GaloisFieldOperations {

  /// 2^8

  public static byte add(byte a, byte b) {
    return (byte) (a ^ b);
  }

  public static byte sub(byte a, byte b) {
    return (byte) (a ^ b);
  }

  /// 2^11 and 2^13

  public static int mult(int a, int b, int g, int n) {
    int res = 0;

    while (b != 0) {
      if ((b & 0x01) != 0) {
        res ^= a;
      }
      a <<= 1;

      if (a >= n) {
        a ^= g;
      }
      b >>>= 1;
    }

    return res;
  }

  public static byte exp3(int a, int g, int n) {
    if (a == 0) {
      return 0;
    }

    return (byte) mult(a, mult(a, a, g, n), g, n);
  }

}
