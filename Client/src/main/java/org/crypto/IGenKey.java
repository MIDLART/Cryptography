package org.crypto;

import java.util.List;

public interface IGenKey {
  public List<byte[]> genKey(byte[] inputKey);
}
