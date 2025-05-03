package org.crypto.des;

import org.crypto.SymmetricEncryption;
import org.crypto.des.impl.DEALFeistelFunction;
import org.crypto.des.impl.DEALGenKey;

import static org.crypto.enums.EncryptOrDecrypt.DECRYPT;
import static org.crypto.enums.EncryptOrDecrypt.ENCRYPT;

public class DEAL extends FeistelCipher implements SymmetricEncryption {
  public DEAL(byte[] inputKey) {
    super(new DEALGenKey(inputKey.length), new DEALFeistelFunction(), inputKey);
  }

  @Override
  public byte[] encryption(byte[] text) {
    if (text == null || text.length != 16) {
      throw new IllegalArgumentException("Text must be 16 bytes ");
    }

    return inverseCipher(text, ENCRYPT);
  }

  @Override
  public byte[] decryption(byte[] text) {
    if (text == null || text.length != 16) {
      throw new IllegalArgumentException("Text must be 16 bytes");
    }

    return inverseCipher(text, DECRYPT);
  }

  @Override
  public int getBlockSize() {
    return 16;
  }
}
