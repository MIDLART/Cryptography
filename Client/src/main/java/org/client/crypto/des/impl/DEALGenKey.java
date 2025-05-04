package org.client.crypto.des.impl;

import org.client.crypto.IGenKey;
import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.des.DES;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.client.crypto.enums.PackingMode.NO;
import static org.client.crypto.operations.BitOperations.xor;
import static org.client.crypto.enums.EncryptionMode.CBC;

public class DEALGenKey implements IGenKey {
  private final int keyLength;

  private final long hexKey = 0x1234_5678_90ab_cdefL;
  private final byte[] desKey = ByteBuffer.allocate(Long.BYTES).putLong(hexKey).array();
  private final byte[] C0 = new byte[8];
  private final SymmetricAlgorithm E = new SymmetricAlgorithm(new DES(desKey), CBC, NO, C0);

  public DEALGenKey(int keyLength) {
    if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
      throw new IllegalArgumentException("Invalid key length: " + keyLength);
    }

    this.keyLength = keyLength;
  }

  @Override
  public List<byte[]> genKey(byte[] inputKey) {
    List<byte[]> K = getK(inputKey);

    return switch (keyLength) {
      case 16 -> genKey128(K);
      case 24 -> genKey192(K);
      case 32 -> genKey256(K);
      default -> throw new IllegalStateException("Unexpected value: " + keyLength);
    };
  }

  private List<byte[]> genKey128 (List<byte[]> K) {
    List<byte[]> RK = new ArrayList<>();

    byte[] c = firstConst();
    int shift = 1;

    RK.add(E.encrypt(K.get(0)));
    RK.add(E.encrypt(xor(K.get(1), RK.get(0))));

    for (int i = 2; i < 6; i++) {
      RK.add(E.encrypt(xor(
              xor(K.get(i % 2), c),
              RK.get(i - 1))));

      nextConst(c, shift);
      shift *= 2;
    }

    return RK;
  }

  private List<byte[]> genKey192 (List<byte[]> K) {
    List<byte[]> RK = new ArrayList<>();

    byte[] c = firstConst();
    int shift = 1;

    RK.add(E.encrypt(K.get(0)));
    RK.add(E.encrypt(xor(K.get(1), RK.get(0))));
    RK.add(E.encrypt(xor(K.get(2), RK.get(1))));

    for (int i = 3; i < 6; i++) {
      RK.add(E.encrypt(xor(
              xor(K.get(i - 3), c),
              RK.get(i - 1))));

      nextConst(c, shift);
      shift *= 2;
    }

    return RK;
  }

  private List<byte[]> genKey256 (List<byte[]> K) {
    List<byte[]> RK = new ArrayList<>();

    byte[] c = firstConst();
    int shift = 1;

    RK.add(E.encrypt(K.get(0)));

    for (int i = 0; i < 3; i++) {
      RK.add(E.encrypt(xor(K.get(i + 1), RK.get(i))));
    }

    for (int i = 3; i < 7; i++) {
      RK.add(E.encrypt(xor(
              xor(K.get(i - 3), c),
              RK.get(i))));

      nextConst(c, shift);
      shift *= 2;
    }

    return RK;
  }

  private List<byte[]> getK (byte[] inputKey) {
    List<byte[]> K = new ArrayList<>();

    K.add(Arrays.copyOfRange(inputKey, 0, 8));
    K.add(Arrays.copyOfRange(inputKey, 8, 16));

    if (keyLength == 24) {
      K.add(Arrays.copyOfRange(inputKey, 16, 24));
    } else if (keyLength == 32) {
      K.add(Arrays.copyOfRange(inputKey, 16, 24));
      K.add(Arrays.copyOfRange(inputKey, 24, 32));
    }

    return K;
  }

  private byte[] firstConst() {
    byte[] c = new byte[8];
    c[0] = (byte) 0b1000_0000;

    return c;
  }

  private void nextConst(byte[] c, int i) {
    c[0] = (byte) (Byte.toUnsignedInt(c[0]) >>> i);
  }
}
