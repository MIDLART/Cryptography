package testsCW;

import org.client.crypto.SymmetricEncryption;
import org.client.crypto.rc5.RC5;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

public class RC5Test {

  @Test
  void testRC5Encryption1() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    byte[] input = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    byte[] expectedOutput = {
            (byte) 0x21, (byte) 0xA5, (byte) 0xDB, (byte) 0xEE,
            (byte) 0x15, (byte) 0x4B, (byte) 0x8F, (byte) 0x6D
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.encryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Decryption1() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    byte[] input = {
            (byte) 0x21, (byte) 0xA5, (byte) 0xDB, (byte) 0xEE,
            (byte) 0x15, (byte) 0x4B, (byte) 0x8F, (byte) 0x6D
    };
    byte[] expectedOutput = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.decryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Encryption2() {
    // SETUP
    byte[] key = {
            (byte) 0x91, (byte) 0x5F, (byte) 0x46, (byte) 0x19,
            (byte) 0xBE, (byte) 0x41, (byte) 0xB2, (byte) 0x51,
            (byte) 0x63, (byte) 0x55, (byte) 0xA5, (byte) 0x01,
            (byte) 0x10, (byte) 0xA9, (byte) 0xCE, (byte) 0x91
    };
    byte[] input = {
            (byte) 0x21, (byte) 0xA5, (byte) 0xDB, (byte) 0xEE,
            (byte) 0x15, (byte) 0x4B, (byte) 0x8F, (byte) 0x6D
    };
    byte[] expectedOutput = {
            (byte) 0xF7, (byte) 0xC0, (byte) 0x13, (byte) 0xAC,
            (byte) 0x5B, (byte) 0x2B, (byte) 0x89, (byte) 0x52
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.encryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Decryption2() {
    // SETUP
    byte[] key = {
            (byte) 0x91, (byte) 0x5F, (byte) 0x46, (byte) 0x19,
            (byte) 0xBE, (byte) 0x41, (byte) 0xB2, (byte) 0x51,
            (byte) 0x63, (byte) 0x55, (byte) 0xA5, (byte) 0x01,
            (byte) 0x10, (byte) 0xA9, (byte) 0xCE, (byte) 0x91
    };
    byte[] input = {
            (byte) 0xF7, (byte) 0xC0, (byte) 0x13, (byte) 0xAC,
            (byte) 0x5B, (byte) 0x2B, (byte) 0x89, (byte) 0x52
    };
    byte[] expectedOutput = {
            (byte) 0x21, (byte) 0xA5, (byte) 0xDB, (byte) 0xEE,
            (byte) 0x15, (byte) 0x4B, (byte) 0x8F, (byte) 0x6D
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.decryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Encryption3() {
    // SETUP
    byte[] key = {
            (byte) 0x78, (byte) 0x33, (byte) 0x48, (byte) 0xE7,
            (byte) 0x5A, (byte) 0xEB, (byte) 0x0F, (byte) 0x2F,
            (byte) 0xD7, (byte) 0xB1, (byte) 0x69, (byte) 0xBB,
            (byte) 0x8D, (byte) 0xC1, (byte) 0x67, (byte) 0x87
    };
    byte[] input = {
            (byte) 0xF7, (byte) 0xC0, (byte) 0x13, (byte) 0xAC,
            (byte) 0x5B, (byte) 0x2B, (byte) 0x89, (byte) 0x52
    };
    byte[] expectedOutput = {
            (byte) 0x2F, (byte) 0x42, (byte) 0xB3, (byte) 0xB7,
            (byte) 0x03, (byte) 0x69, (byte) 0xFC, (byte) 0x92
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.encryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Encryption4() {
    // SETUP
    byte[] key = {
            (byte) 0xDC, (byte) 0x49, (byte) 0xDB, (byte) 0x13,
            (byte) 0x75, (byte) 0xA5, (byte) 0x58, (byte) 0x4F,
            (byte) 0x64, (byte) 0x85, (byte) 0xB4, (byte) 0x13,
            (byte) 0xB5, (byte) 0xF1, (byte) 0x2B, (byte) 0xAF
    };
    byte[] input = {
            (byte) 0x2F, (byte) 0x42, (byte) 0xB3, (byte) 0xB7,
            (byte) 0x03, (byte) 0x69, (byte) 0xFC, (byte) 0x92
    };
    byte[] expectedOutput = {
            (byte) 0x65, (byte) 0xC1, (byte) 0x78, (byte) 0xB2,
            (byte) 0x84, (byte) 0xD1, (byte) 0x97, (byte) 0xCC
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.encryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Encryption5() {
    // SETUP
    byte[] key = {
            (byte) 0x52, (byte) 0x69, (byte) 0xF1, (byte) 0x49,
            (byte) 0xD4, (byte) 0x1B, (byte) 0xA0, (byte) 0x15,
            (byte) 0x24, (byte) 0x97, (byte) 0x57, (byte) 0x4D,
            (byte) 0x7F, (byte) 0x15, (byte) 0x31, (byte) 0x25
    };
    byte[] input = {
            (byte) 0x65, (byte) 0xC1, (byte) 0x78, (byte) 0xB2,
            (byte) 0x84, (byte) 0xD1, (byte) 0x97, (byte) 0xCC
    };
    byte[] expectedOutput = {
            (byte) 0xEB, (byte) 0x44, (byte) 0xE4, (byte) 0x15,
            (byte) 0xDA, (byte) 0x31, (byte) 0x98, (byte) 0x24
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(32, 12, 16, key);
    byte[] actualOutput = rc5.encryption(input);

    System.out.println(Arrays.toString(actualOutput));
    System.out.println(Arrays.toString(expectedOutput));

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testRC5Cycle16() {
    // SETUP
    byte[] key = {
            (byte) 0xA4, (byte) 0x78, (byte) 0x74, (byte) 0x86,
            (byte) 0xEF, (byte) 0xED, (byte) 0x04, (byte) 0x96,
            (byte) 0x28, (byte) 0x63, (byte) 0xAF, (byte) 0xD5,
            (byte) 0x5A, (byte) 0x37, (byte) 0xFB, (byte) 0xC4,
    };
    byte[] message = {
            (byte) 0x55, (byte) 0xB8, (byte) 0xAC, (byte) 0x01,
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(key, 4);
    byte[] decryptedMessage = rc5.decryption(rc5.encryption(message));

    // ASSERTION
    Assert.assertEquals(decryptedMessage, message);
  }

  @Test
  void testRC5Cycle32() {
    // SETUP
    byte[] key = {
            (byte) 0xA4, (byte) 0x78, (byte) 0x74, (byte) 0x86,
            (byte) 0xEF, (byte) 0xED, (byte) 0x04, (byte) 0x96,
            (byte) 0x28, (byte) 0x63, (byte) 0xAF, (byte) 0xD5,
            (byte) 0x5A, (byte) 0x37, (byte) 0xFB, (byte) 0xC4,
    };
    byte[] message = {
            (byte) 0x55, (byte) 0xB8, (byte) 0xAC, (byte) 0x01,
            (byte) 0x1F, (byte) 0xBF, (byte) 0xAA, (byte) 0x00,
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(key, 8);
    byte[] decryptedMessage = rc5.decryption(rc5.encryption(message));

    // ASSERTION
    Assert.assertEquals(decryptedMessage, message);
  }

  @Test
  void testRC5Cycle64() {
    // SETUP
    byte[] key = {
            (byte) 0xA4, (byte) 0x78, (byte) 0x74, (byte) 0x86,
            (byte) 0xEF, (byte) 0xED, (byte) 0x04, (byte) 0x96,
            (byte) 0x28, (byte) 0x63, (byte) 0xAF, (byte) 0xD5,
            (byte) 0x5A, (byte) 0x37, (byte) 0xFB, (byte) 0xC4,
    };
    byte[] message = {
            (byte) 0x55, (byte) 0xB8, (byte) 0xAC, (byte) 0x01,
            (byte) 0x1F, (byte) 0xBF, (byte) 0xAA, (byte) 0x00,
            (byte) 0x34, (byte) 0x12, (byte) 0xA6, (byte) 0x26,
            (byte) 0x45, (byte) 0x77, (byte) 0xEF, (byte) 0x9C,
    };

    // EXECUTION
    SymmetricEncryption rc5 = new RC5(key, 16);
    byte[] decryptedMessage = rc5.decryption(rc5.encryption(message));

    // ASSERTION
    Assert.assertEquals(decryptedMessage, message);
  }
}
