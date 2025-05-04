package org.client.diffie_hellman;

import java.math.BigInteger;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    var dhA = new DiffieHellmanProtocol();

    BigInteger A = dhA.getA();
    BigInteger p = dhA.getP();
    BigInteger g = dhA.getG();

    var dhB = new DiffieHellmanProtocol(p, g);

    BigInteger B = dhB.getA();

    System.out.println(Arrays.toString(dhA.getKey(B)));
    System.out.println(Arrays.toString(dhB.getKey(A)));
  }
}