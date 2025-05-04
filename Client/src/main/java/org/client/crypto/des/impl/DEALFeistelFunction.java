package org.client.crypto.des.impl;

import org.client.crypto.IFeistelFunction;
import org.client.crypto.des.DES;

public class DEALFeistelFunction implements IFeistelFunction {
  @Override
  public byte[] encrypt(byte[] text, byte[] key) {
    DES des = new DES(key);
    return des.encryption(text);
  }
}
