package org.client.crypto.des;

import org.client.crypto.IFeistelFunction;
import org.client.crypto.IGenKey;
import org.client.crypto.enums.EncryptOrDecrypt;

import java.util.List;

import static org.client.crypto.operations.BitOperations.*;
import static org.client.crypto.enums.EncryptOrDecrypt.*;

public class FeistelCipher {
  private final IFeistelFunction encryption;
  List<byte[]> keys;

  public FeistelCipher(IGenKey genKey, IFeistelFunction encryption, byte[] inputKey) {
    this.encryption = encryption;
    keys = genKey.genKey(inputKey);
  }

  public byte[] cipher(byte[] text, EncryptOrDecrypt encryptOrDecrypt) {
    int halfLength = text.length / 2;

    byte[] L = new byte[halfLength];
    byte[] R = new byte[halfLength];

    System.arraycopy(text, 0, L, 0, halfLength);
    System.arraycopy(text, halfLength, R, 0, halfLength);

    cipherBase(L, R, halfLength, encryptOrDecrypt);

    return concatByteArrays(R, L);
  }

  public byte[] inverseCipher(byte[] text, EncryptOrDecrypt encryptOrDecrypt) {
    int halfLength = text.length / 2;

    byte[] L = new byte[halfLength];
    byte[] R = new byte[halfLength];

    System.arraycopy(text, 0, L, 0, halfLength);
    System.arraycopy(text, halfLength, R, 0, halfLength);

    if (encryptOrDecrypt == EncryptOrDecrypt.DECRYPT) {
      cipherBase(L, R, halfLength, encryptOrDecrypt);
    } else {
      cipherBase(R, L, halfLength, encryptOrDecrypt);
    }

    return concatByteArrays(L, R);
  }

  private void cipherBase(byte[] L, byte[] R, int halfLength, EncryptOrDecrypt encryptOrDecrypt) {
    byte[] tmp = new byte[halfLength];

    int start = 0;
    int end = keys.size();
    int step = 1;

    if (encryptOrDecrypt == DECRYPT) {
      start = keys.size() - 1;
      end = -1;
      step = -1;
    }

    for (int i = start; i != end; i += step) {
      System.arraycopy(R, 0, tmp, 0, halfLength);
      System.arraycopy(xor(L, encryption.encrypt(R, keys.get(i))), 0, R, 0, halfLength);
      System.arraycopy(tmp, 0, L, 0, halfLength);
    }
  }
}
