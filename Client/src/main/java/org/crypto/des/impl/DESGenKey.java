package org.crypto.des.impl;

import org.crypto.IGenKey;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.crypto.operations.BitOperations.*;
import static org.crypto.enums.IndexingRule.HIGHEST1;
import static org.crypto.permutations.Permutations.rearrange;
import static org.crypto.permutations.matrices.MGenKey.*;

public class DESGenKey implements IGenKey {
  @Override
  public List<byte[]> genKey(byte[] inputKey) {
    List<byte[]> res = new ArrayList<>(16);

    int len = inputKey.length;
    if (len != 7 && len != 8) {
      throw new IllegalArgumentException("Input key must be 7 or 8 bytes");
    }

    byte[] extKey;
    if (len == 7) {
      extKey = addingBits(inputKey);
    } else {
      extKey = inputKey;
    }

    extKey = rearrange(extKey, startP, HIGHEST1);
    byte [] extKeyR = new byte[7];
    System.arraycopy(extKey, 0, extKeyR, 0, 7);

    byte[] C = getC(extKeyR);
    byte[] D = getD(extKeyR);

    List<byte[]> CD = new ArrayList<>(16);

    for (int i = 0; i < 16; i++) {
      if (i == 0 || i == 1 || i == 8 || i == 15) {
        cyclicShiftC(C, 1);
        cyclicShiftD(D, 1);
      } else {
        cyclicShiftC(C, 2);
        cyclicShiftD(D, 2);
      }

      CD.add(concatCD(C, D));
    }

    for (int i = 0; i < 16; i++) {
      res.add(rearrange(CD.get(i), endP, HIGHEST1));
    }

    return res;
  }

  private static void shutdown(ExecutorService executor) {
    executor.shutdown();

    try {
      if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      throw new RuntimeException(e);
    }
  }

  private byte[] addingBits (byte[] inputKey) {
    byte[] res = new byte[8];

    int ind = 0;
    int unitsCount = 0;
    byte bit;

    for (int i = 0; i < 64; i++) {
      if (i % 8 == 7) {
        res[i / 8] |= (byte) (1 - unitsCount % 2);
        unitsCount = 0;
      } else {
        bit = bitAt(ind, inputKey);
        res[i / 8] |= (byte) (bit << (7 - i % 8));

        ++ind;
        if (bit == 1) {
          ++unitsCount;
        }
      }
    }

    return res;
  }

  private byte[] getC(byte[] key) {
    byte[] C = new byte[4];

    System.arraycopy(key, 0, C, 0, 4);
    C[3] &= zeroMaskLow(4);

    return C;
  }

  private byte[] getD(byte[] key) {
    byte[] D = new byte[4];

    System.arraycopy(key, 3, D, 0, 4);
    D[0] &= zeroMaskHigh(4);

    return D;
  }

  private void cyclicShiftC(byte[] C, int k) {
    byte rem = (byte) ((Byte.toUnsignedInt(C[0]) >>> (8 - k)) << 4);

    for (int i = 0; i < 4; i++) {
      C[i] <<= k;

      if (i != 3) {
        C[i] |= (byte) (Byte.toUnsignedInt(C[i + 1]) >>> (8 - k));
      }
    }

    C[3] |= rem;
  }

  private void cyclicShiftD(byte[] D, int k) {
    byte rem = (byte) (Byte.toUnsignedInt(D[0]) >>> (4 - k));

    for (int i = 0; i < 4; i++) {
      D[i] <<= k;

      if (i != 3) {
        D[i] |= (byte) (Byte.toUnsignedInt(D[i + 1]) >>> (8 - k));
      }
    }

    D[0] &= zeroMaskHigh(4);
    D[3] |= rem;
  }

  private static byte[] concatCD(byte[] C, byte[] D) {
    byte[] res = new byte[7];

    System.arraycopy(C, 0, res, 0, 4);
    System.arraycopy(D, 1, res, 4, 3);
    res[3] |= D[0];

    return res;
  }
}
