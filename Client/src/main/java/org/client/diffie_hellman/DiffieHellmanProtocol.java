package org.client.diffie_hellman;

import lombok.Getter;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DiffieHellmanProtocol {
  private final SecureRandom random = new SecureRandom();

  @Getter
  private BigInteger p;
  @Getter
  private BigInteger g;

  private final BigInteger a;
  @Getter
  private final BigInteger A;

  public DiffieHellmanProtocol() {
    do {
      p = BigInteger.probablePrime(256, random);
    } while (!p.isProbablePrime(100));

    do {
      g = BigInteger.probablePrime(8, random);
    } while (!g.isProbablePrime(100));

    a = new BigInteger(256 - 1, random);
    A = g.modPow(a, p);
  }

  public DiffieHellmanProtocol(BigInteger p, BigInteger g) {
    this.p = p;
    this.g = g;

    a = new BigInteger(256 - 1, random);
    A = g.modPow(a, p);
  }

  public byte[] getKey(BigInteger B) {
    BigInteger K = B.modPow(a, p);

    return K.toByteArray();
  }
}
