package org.client.crypto.loki97.impl;

import static org.client.crypto.enums.IndexingRule.LOWEST0;
import static org.client.crypto.operations.BitOperations.getBits;
import static org.client.crypto.permutations.Permutations.rearrange;
import static org.client.crypto.permutations.matrices.MLOKI97.LOKI97Permutation;

public class LOKI97FeistelFunction {
  private final byte[] S1;
  private final byte[] S2;

  public LOKI97FeistelFunction(byte[] s1, byte[] s2) {
    S1 = s1;
    S2 = s2;
  }

  public long encrypt(long A, long B) { //B == SKr
    return Sb(P(Sa(E(KP(A, B)))), B);
  }

  private long KP(long A, long B) {
    int Al = (int) (A >>> 32);
    int Ar = (int) A;

    int SKr = (int) B;

    return ((long) ((Al & ~SKr) | (Ar & SKr)) << 32) | (((Ar & ~SKr) | (Al & SKr)) & 0xFFFFFFFFL);
  }

  private int[] E(long A) {
    int[] res = new int[8];

    res[0] = (int) ((getBits(A, 4, 0) << 8) | getBits(A, 63, 56));
    res[1] = (int) (getBits(A, 58, 48));
    res[2] = (int) (getBits(A, 52, 40));
    res[3] = (int) (getBits(A, 42, 32));
    res[4] = (int) (getBits(A, 34, 24));
    res[5] = (int) (getBits(A, 28, 16));
    res[6] = (int) (getBits(A, 18, 8));
    res[7] = (int) (getBits(A, 12, 0));

    return res;
  }

  private byte[] Sa(int[] blocks) {
    byte[] res = new byte[8];

    res[0] = S1[blocks[0]];
    res[1] = S2[blocks[1]];
    res[2] = S1[blocks[2]];
    res[3] = S2[blocks[3]];
    res[4] = S2[blocks[4]];
    res[5] = S1[blocks[5]];
    res[6] = S2[blocks[6]];
    res[7] = S1[blocks[7]];

    return res;
  }

  private long Sb(byte[] blocks, long B) {
    long res;

    int[] SKl = getSKl(B);

    res =  Byte.toUnsignedLong(S2[(SKl[0] << 8) | Byte.toUnsignedInt(blocks[0])]) << 56;
    res |= Byte.toUnsignedLong(S2[(SKl[1] << 8) | Byte.toUnsignedInt(blocks[1])]) << 48;
    res |= Byte.toUnsignedLong(S1[(SKl[2] << 8) | Byte.toUnsignedInt(blocks[2])]) << 40;
    res |= Byte.toUnsignedLong(S1[(SKl[3] << 8) | Byte.toUnsignedInt(blocks[3])]) << 32;
    res |= Byte.toUnsignedLong(S2[(SKl[4] << 8) | Byte.toUnsignedInt(blocks[4])]) << 24;
    res |= Byte.toUnsignedLong(S2[(SKl[5] << 8) | Byte.toUnsignedInt(blocks[5])]) << 16;
    res |= Byte.toUnsignedLong(S1[(SKl[6] << 8) | Byte.toUnsignedInt(blocks[6])]) << 8;
    res |= Byte.toUnsignedLong(S1[(SKl[7] << 8) | Byte.toUnsignedInt(blocks[7])]);

    return res;
  }

  private byte[] P(byte[] blocks) {
    return rearrange(blocks, LOKI97Permutation, LOWEST0);
  }

  private int[] getSKl(long B) {
    int[] res = new int[8];

    res[0] = (int) (getBits(B, 63, 61));
    res[1] = (int) (getBits(B, 60, 58));
    res[2] = (int) (getBits(B, 57, 53));
    res[3] = (int) (getBits(B, 52, 48));
    res[4] = (int) (getBits(B, 47, 45));
    res[5] = (int) (getBits(B, 44, 42));
    res[6] = (int) (getBits(B, 41, 37));
    res[7] = (int) (getBits(B, 36, 32));

    return res;
  }
}
