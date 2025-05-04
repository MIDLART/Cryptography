package org.client.crypto.des;

import org.client.crypto.SymmetricEncryption;
import org.client.crypto.des.impl.DESFeistelFunction;
import org.client.crypto.des.impl.DESGenKey;

import static org.client.crypto.enums.IndexingRule.HIGHEST1;
import static org.client.crypto.permutations.Permutations.rearrange;
import static org.client.crypto.permutations.matrices.MDES.*;
import static org.client.crypto.enums.EncryptOrDecrypt.*;

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