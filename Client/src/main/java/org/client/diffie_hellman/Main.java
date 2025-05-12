package org.client.diffie_hellman;

import java.math.BigInteger;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {
    var dhA = new DiffieHellmanProtocol();

    BigInteger A = dhA.getPublicA();

    var dhB = new DiffieHellmanProtocol();

    BigInteger B = dhB.getPublicA();

    System.out.println(Arrays.toString(dhA.getKey(B)));
    System.out.println(Arrays.toString(dhB.getKey(A)));
  }
}