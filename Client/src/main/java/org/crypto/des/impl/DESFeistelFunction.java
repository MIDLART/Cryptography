package org.crypto.des.impl;

import org.crypto.IFeistelFunction;

import static org.crypto.operations.BitOperations.*;
import static org.crypto.enums.IndexingRule.HIGHEST1;
import static org.crypto.permutations.Permutations.rearrange;
import static org.crypto.permutations.matrices.MEncryption.*;

public class DESFeistelFunction implements IFeistelFunction {
  @Override
  public byte[] encrypt(byte[] text, byte[] key) {
    if (text.length != 4 || key.length != 6) {
      throw new IllegalArgumentException("Wrong length");
    }

    byte[] ER = rearrange(text, E, HIGHEST1);
    byte[] B = blocksB(xor(ER, key));
    byte[] SB = conversionS(B);

    return rearrange(gluingB(SB), P, HIGHEST1);
  }

  private byte[] blocksB(byte[] text) {
    byte[] res = new byte[8];

    for(int i = 0; i < 48; i++){
      res[i / 6] |= (byte) (bitAt(i , text) << (5 - i % 6));
    }

    return res;
  }

  private byte[] conversionS(byte[] B) {
    byte[] res = new byte[8];

    for (int j = 0; j < 8; j++) {
      int column = (zeroMaskHigh(3) & B[j]) >>> 1;
      int row = (bitAt(2, B[j]) << 1) | bitAt(7, B[j]);

      res[j] = (byte) S.get(j)[row][column];
    }

    return res;
  }

  private byte[] gluingB(byte[] B) {
    byte[] res = new byte[4];

    for (int i = 0; i < 4; i++) {
      res[i] = (byte) ((B[2 * i] << 4) | B[2 * i + 1]);
    }

    return res;
  }
}
