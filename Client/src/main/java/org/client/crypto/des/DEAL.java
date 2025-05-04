package org.client.crypto.des;

import org.client.crypto.SymmetricEncryption;
import org.client.crypto.des.impl.DEALFeistelFunction;
import org.client.crypto.des.impl.DEALGenKey;

import static org.client.crypto.enums.EncryptOrDecrypt.DECRYPT;
import static org.client.crypto.enums.EncryptOrDecrypt.ENCRYPT;

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
