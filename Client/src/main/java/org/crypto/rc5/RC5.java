package org.crypto.rc5;

import org.crypto.SymmetricEncryption;
import org.crypto.rc5.impl.RC5FeistelCipher;

public class RC5 extends RC5FeistelCipher implements SymmetricEncryption {
  private final int blockSize;

  public RC5(int w, int r, int b, byte[] inputKey) {
    super(w, r, inputKey);

    if (b != inputKey.length) {
      throw new IllegalArgumentException("Input key length does not match input key length");
    }

    blockSize = w * 2 / 8;
  }

  public RC5(byte[] inputKey, int blockSize) {
    super(blockSize / 2 * 8, 12, inputKey);

    this.blockSize = blockSize;
  }

  @Override
  public byte[] encryption(byte[] text) {
    if (text == null || text.length != blockSize) {
      throw new IllegalArgumentException("Text must be " + blockSize + " bytes");
    }

    return encrypt(text);
  }

  @Override
  public byte[] decryption(byte[] text) {
    if (text == null || text.length != blockSize) {
      throw new IllegalArgumentException("Text must be " + blockSize + " bytes");
    }

    return decrypt(text);
  }

  @Override
  public int getBlockSize() {
    return blockSize;
  }
}
