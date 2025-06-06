package org.client.diffie_hellman;

import lombok.Getter;

import java.math.BigInteger;
import java.security.SecureRandom;

public class DiffieHellmanProtocol {
  private final SecureRandom random = new SecureRandom();

  String hexString =
          "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
          "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
          "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
          "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
          "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
          "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
          "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
          "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";

  private BigInteger p = new BigInteger(hexString, 16);
  private BigInteger g = BigInteger.valueOf(2);
  @Getter
  private final BigInteger a;
  @Getter
  private final BigInteger publicA;

  public DiffieHellmanProtocol() {
    a = new BigInteger(511, random).setBit(511);
    publicA = g.modPow(a, p);
  }

  public DiffieHellmanProtocol(BigInteger a) {
    this.a = a;
    publicA = null;
  }

  public byte[] getKey(BigInteger B) {
    BigInteger K = B.modPow(a, p);

    return K.toByteArray();
  }
}
