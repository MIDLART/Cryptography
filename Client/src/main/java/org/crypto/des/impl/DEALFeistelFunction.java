package org.crypto.des.impl;

import org.crypto.IFeistelFunction;
import org.crypto.des.DES;

public class DEALFeistelFunction implements IFeistelFunction {
  @Override
  public byte[] encrypt(byte[] text, byte[] key) {
    DES des = new DES(key);
    return des.encryption(text);
  }
}
