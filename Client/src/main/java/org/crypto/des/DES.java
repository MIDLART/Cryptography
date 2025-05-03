package org.crypto.des;

import org.crypto.SymmetricEncryption;
import org.crypto.des.impl.DESFeistelFunction;
import org.crypto.des.impl.DESGenKey;

import static org.crypto.enums.IndexingRule.HIGHEST1;
import static org.crypto.permutations.Permutations.rearrange;
import static org.crypto.permutations.matrices.MDES.*;
import static org.crypto.enums.EncryptOrDecrypt.*;

public class DES extends FeistelCipher implements SymmetricEncryption {
  public DES(byte[] inputKey) {
    super(new DESGenKey(), new DESFeistelFunction(), inputKey);
  }

  @Override
  public byte[] encryption(byte[] text) {
    if (text == null || text.length != 8) {
      throw new IllegalArgumentException("Text must be 8 bytes");
    }

    return rearrange(
            cipher(rearrange(text, IP, HIGHEST1), ENCRYPT),
            reverseIP, HIGHEST1);
  }

  @Override
  public byte[] decryption(byte[] text) {
    if (text == null || text.length != 8) {
      throw new IllegalArgumentException("Text must be 8 bytes");
    }

    return rearrange(
            cipher(rearrange(text, IP, HIGHEST1), DECRYPT),
            reverseIP, HIGHEST1);
  }

  @Override
  public int getBlockSize() {
    return 8;
  }
}