package org.crypto.loki97;

import org.crypto.SymmetricEncryption;
import org.crypto.loki97.impl.LOKI97FeistelCipher;

public class LOKI97 extends LOKI97FeistelCipher implements SymmetricEncryption {
  public LOKI97(byte[] inputKey, byte[] s1, byte[] s2) {
    super(inputKey, s1, s2);
  }

  @Override
  public byte[] encryption(byte[] text) {
    if (text == null || text.length != 16) {
      throw new IllegalArgumentException("Text must be " + 16 + " bytes");
    }

    return encrypt(text);
  }

  @Override
  public byte[] decryption(byte[] text) {
    if (text == null || text.length != 16) {
      throw new IllegalArgumentException("Text must be " + 16 + " bytes");
    }

    return decrypt(text);
  }

  @Override
  public int getBlockSize() {
    return 16;
  }
}
