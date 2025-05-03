package org.crypto;

public interface IFeistelFunction {
  public byte[] encrypt(final byte[] text, final byte[] key);
}
