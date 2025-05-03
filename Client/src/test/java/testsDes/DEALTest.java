package testsDes;

import org.crypto.IFeistelFunction;
import org.crypto.IGenKey;
import org.crypto.SymmetricEncryption;
import org.crypto.des.DEAL;
import org.crypto.des.impl.DEALFeistelFunction;
import org.crypto.des.impl.DEALGenKey;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;

public class DEALTest {

  @Test
  void testGenKey128() {
    // SETUP
    byte[] key = { // A4787486EFED04962863AFD5A537FBC4
            (byte) 0xA4, (byte) 0x78, (byte) 0x74, (byte) 0x86, (byte) 0xEF, (byte) 0xED, (byte) 0x04, (byte) 0x96,
            (byte) 0x28, (byte) 0x63, (byte) 0xAF, (byte) 0xD5, (byte) 0x5A, (byte) 0x37, (byte) 0xFB, (byte) 0xC4,
    };
    byte[][] expectedRoundKeys = {
            { (byte) 0xF6, (byte) 0x9F, (byte) 0x7A, (byte) 0xCA, (byte) 0x41, (byte) 0xBA, (byte) 0x25, (byte) 0xD7 },
            { (byte) 0xB2, (byte) 0x24, (byte) 0xE3, (byte) 0xBC, (byte) 0x08, (byte) 0xA7, (byte) 0x08, (byte) 0x7C },
            { (byte) 0x42, (byte) 0xB1, (byte) 0xB8, (byte) 0xCB, (byte) 0x30, (byte) 0xE5, (byte) 0xA4, (byte) 0x17 },
            { (byte) 0x71, (byte) 0x21, (byte) 0xF3, (byte) 0xBC, (byte) 0x1A, (byte) 0xCF, (byte) 0x41, (byte) 0x32 },
            { (byte) 0x86, (byte) 0x2C, (byte) 0x21, (byte) 0x29, (byte) 0x0F, (byte) 0x4B, (byte) 0xCD, (byte) 0x08 },
            { (byte) 0x2A, (byte) 0xFC, (byte) 0x98, (byte) 0xCE, (byte) 0x46, (byte) 0xF1, (byte) 0x42, (byte) 0xED }
    };

    // EXECUTION
    IGenKey scheduler = new DEALGenKey(key.length);
    byte[][] actualRoundKeys = scheduler.genKey(key).toArray(new byte[0][]);

    // ASSERTION
    Assert.assertEquals(actualRoundKeys, expectedRoundKeys);
  }

  @Test
  void testGenKey192() {
    // SETUP
    byte[] key = { // A91C15211A55450D9723E784A7415E4D61676486F937EDB7
            (byte) 0xA9, (byte) 0x1C, (byte) 0x15, (byte) 0x21, (byte) 0x1A, (byte) 0x55, (byte) 0x45, (byte) 0x0D,
            (byte) 0x97, (byte) 0x23, (byte) 0xE7, (byte) 0x84, (byte) 0xA7, (byte) 0x41, (byte) 0x5E, (byte) 0x4D,
            (byte) 0x61, (byte) 0x67, (byte) 0x64, (byte) 0x86, (byte) 0xF9, (byte) 0x37, (byte) 0xED, (byte) 0xB7,
    };
    byte[][] expectedRoundKeys = {
            { (byte) 0x7F, (byte) 0x1A, (byte) 0xAF, (byte) 0x36, (byte) 0xF3, (byte) 0x86, (byte) 0xC8, (byte) 0xCD },
            { (byte) 0xFA, (byte) 0xCB, (byte) 0x35, (byte) 0x8D, (byte) 0x46, (byte) 0x0C, (byte) 0xFF, (byte) 0x5E },
            { (byte) 0x38, (byte) 0x26, (byte) 0xFD, (byte) 0x9A, (byte) 0xD0, (byte) 0xC1, (byte) 0x50, (byte) 0xC4 },
            { (byte) 0xC1, (byte) 0x26, (byte) 0x8E, (byte) 0x3C, (byte) 0xD6, (byte) 0xD9, (byte) 0x85, (byte) 0x91 },
            { (byte) 0xE4, (byte) 0x90, (byte) 0x9E, (byte) 0x6E, (byte) 0xCE, (byte) 0x1E, (byte) 0x3A, (byte) 0x65 },
            { (byte) 0xCC, (byte) 0x16, (byte) 0xE4, (byte) 0x3A, (byte) 0x46, (byte) 0x56, (byte) 0x04, (byte) 0x0A }
    };

    // EXECUTION
    IGenKey scheduler = new DEALGenKey(key.length);
    byte[][] actualRoundKeys = scheduler.genKey(key).toArray(new byte[0][]);

    // ASSERTION
    Assert.assertEquals(actualRoundKeys, expectedRoundKeys);
  }

  @Test
  void testGenKey256() {
    // SETUP
    byte[] key = { // 17DE7A9287157CA263A7035E5B7BAFE2716FB118DDDD563E6AE9FFAE1EF86FBB
            (byte) 0x17, (byte) 0xDE, (byte) 0x7A, (byte) 0x92, (byte) 0x87, (byte) 0x15, (byte) 0x7C, (byte) 0xA2,
            (byte) 0x63, (byte) 0xA7, (byte) 0x03, (byte) 0x5E, (byte) 0x5B, (byte) 0x7B, (byte) 0xAF, (byte) 0xE2,
            (byte) 0x71, (byte) 0x6F, (byte) 0xB1, (byte) 0x18, (byte) 0xDD, (byte) 0xDD, (byte) 0x56, (byte) 0x3E,
            (byte) 0x6A, (byte) 0xE9, (byte) 0xFF, (byte) 0xAE, (byte) 0x1E, (byte) 0xF8, (byte) 0x6F, (byte) 0xBB,
    };
    byte[][] expectedRoundKeys = {
            { (byte) 0x3D, (byte) 0xF8, (byte) 0xD4, (byte) 0x27, (byte) 0x44, (byte) 0x0C, (byte) 0x09, (byte) 0x72 },
            { (byte) 0x99, (byte) 0xD1, (byte) 0x65, (byte) 0x8D, (byte) 0x2E, (byte) 0x50, (byte) 0xC3, (byte) 0x39 },
            { (byte) 0x01, (byte) 0xFF, (byte) 0xCF, (byte) 0x7B, (byte) 0x16, (byte) 0x35, (byte) 0xE6, (byte) 0x04 },
            { (byte) 0x13, (byte) 0x90, (byte) 0xC2, (byte) 0x3E, (byte) 0x72, (byte) 0x49, (byte) 0x3B, (byte) 0x97 },
            { (byte) 0x7A, (byte) 0xB2, (byte) 0xE2, (byte) 0x83, (byte) 0x23, (byte) 0x9B, (byte) 0xA4, (byte) 0xEF },
            { (byte) 0xBC, (byte) 0x73, (byte) 0xAC, (byte) 0x98, (byte) 0xF9, (byte) 0x4E, (byte) 0xD0, (byte) 0x02 },
            { (byte) 0x5D, (byte) 0xDA, (byte) 0x95, (byte) 0x92, (byte) 0xD5, (byte) 0x11, (byte) 0xE2, (byte) 0x39 },
            { (byte) 0x4A, (byte) 0x81, (byte) 0xB0, (byte) 0xB4, (byte) 0x44, (byte) 0xB9, (byte) 0x69, (byte) 0xC7 }
    };

    // EXECUTION
    IGenKey scheduler = new DEALGenKey(key.length);
    byte[][] actualRoundKeys = scheduler.genKey(key).toArray(new byte[0][]);

    // ASSERTION
    Assert.assertEquals(actualRoundKeys, expectedRoundKeys);
  }

  @Test
  void testFeistelFunction() {
    // SETUP
    byte[] roundKey = {
        (byte) 0x43, (byte) 0x62, (byte) 0xE6, (byte) 0x53, (byte) 0x1C, (byte) 0xA9, (byte) 0x0B
    };
    byte[] input = {
        (byte) 0x54, (byte) 0xE5, (byte) 0xEF, (byte) 0x8D, (byte) 0x21, (byte) 0x7A, (byte) 0x32, (byte) 0xA6
    };
    byte[] expectedOutput = {
        (byte) 0xF4, (byte) 0x01, (byte) 0x21, (byte) 0x31, (byte) 0x37, (byte) 0x54, (byte) 0x32, (byte) 0xBF
    };
    
    // EXECUTION
    IFeistelFunction feistelFunction = new DEALFeistelFunction();
    byte[] actualOutput = feistelFunction.encrypt(input, roundKey);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test(dataProvider = "ValidDataForDEAL")
  void testDEALEncryption(byte[] key, byte[] message, byte[] expectedCipher) {
    // SETUP
    byte[] originalMessage = Arrays.copyOf(message, message.length);
    
    // EXECUTION
    SymmetricEncryption deal = new DEAL(key);
    System.out.println("!");
    byte[] actualCipherText = deal.encryption(message);

    // ASSERTION
    Assert.assertEquals(message, originalMessage);
    Assert.assertEquals(actualCipherText, expectedCipher);
  }

  @Test(dataProvider = "ValidDataForDEAL")
  void testDESDecryption(byte[] key, byte[] expectedMessage, byte[] cipher) {
    // SETUP
    byte[] originalCipher = Arrays.copyOf(cipher, cipher.length);

    // EXECUTION
    SymmetricEncryption deal = new DEAL(key);
    byte[] actualMessage = deal.decryption(cipher);

    // ASSERTION
    Assert.assertEquals(cipher, originalCipher);
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test(dataProvider = "ValidDataForDEAL")
  void testDESCycle(byte[] key, byte[] message, byte[] cipher) {
    // EXECUTION
    SymmetricEncryption deal = new DEAL(key);
    byte[] decryptedMessage = deal.decryption(deal.encryption(message));

    // ASSERTION
    Assert.assertEquals(decryptedMessage, message);
  }

  @DataProvider(name = "ValidDataForDEAL")
  Object[][] getValidData() {
    return new byte[][][] {
        {
            { // KEY
                (byte) 0xA4, (byte) 0x78, (byte) 0x74, (byte) 0x86,
                (byte) 0xEF, (byte) 0xED, (byte) 0x04, (byte) 0x96,
                (byte) 0x28, (byte) 0x63, (byte) 0xAF, (byte) 0xD5,
                (byte) 0x5A, (byte) 0x37, (byte) 0xFB, (byte) 0xC4,
            },
            { // MESSAGE
                (byte) 0x5E, (byte) 0xB6, (byte) 0x0C, (byte) 0x37,
                (byte) 0xE3, (byte) 0xC4, (byte) 0xF2, (byte) 0x30,
                (byte) 0xDC, (byte) 0xA8, (byte) 0x2E, (byte) 0x77,
                (byte) 0xBF, (byte) 0x73, (byte) 0xA5, (byte) 0x5C,
            },
            { // CIPHER
                (byte) 0x0B, (byte) 0x71, (byte) 0x5A, (byte) 0x26,
                (byte) 0x2C, (byte) 0xB3, (byte) 0x5A, (byte) 0x6F,
                (byte) 0xB4, (byte) 0x45, (byte) 0x2F, (byte) 0x04,
                (byte) 0x7B, (byte) 0x23, (byte) 0xC3, (byte) 0x0C,
            }
        }
    };
  }
}
