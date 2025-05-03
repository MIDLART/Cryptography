package org.crypto;

public interface SymmetricEncryption {
  public byte[] encryption(byte[] text);

  public byte[] decryption(byte[] text);

  public int getBlockSize();
}
