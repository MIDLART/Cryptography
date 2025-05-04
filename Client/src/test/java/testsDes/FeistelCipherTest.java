package testsDes;

import org.client.crypto.IFeistelFunction;
import org.client.crypto.des.FeistelCipher;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.IntStream;

public class FeistelCipherTest {

  IFeistelFunction feistelFunction = (data, key) -> {
    byte[] res = new byte[data.length];
    IntStream.range(0, data.length).forEach(i -> res[i] = (byte) (data[i] ^ key[i]));
    return res;
  };

  @Test
  void testFeistelCipherEncryption() {
    // SETUP
    List<byte[]> roundKeys = List.of(
            new byte[] { (byte) 0x58, (byte) 0xE2 },
            new byte[] { (byte) 0x42, (byte) 0xE6 }
    );
    byte[] message = {
        (byte) 0x54, (byte) 0x76, (byte) 0x65, (byte) 0x4B
    };
    byte[] expectedCipherText = {
        (byte) 0x4E, (byte) 0x72, (byte) 0x69, (byte) 0xDF
    };

    byte[] key = {(byte) 4};

    // EXECUTION
    FeistelCipher feistelCipher = new FeistelCipher(
        _ -> roundKeys, feistelFunction, key);

    byte[] actualCipherText = feistelCipher.cipher(message, ENCRYPT);

    // ASSERTION
    Assert.assertEquals(actualCipherText, expectedCipherText);
  }

  @Test
  void testFeistelCipherDecryption() {
    // SETUP
    List<byte[]> roundKeys = List.of(
          new byte[] { (byte) 0x58, (byte) 0xE2 },
          new byte[] { (byte) 0x42, (byte) 0xE6 }
    );
    byte[] cipherText = {
        (byte) 0x4E, (byte) 0x72, (byte) 0x69, (byte) 0xDF
    };
    byte[] expectedMessage = {
        (byte) 0x54, (byte) 0x76, (byte) 0x65, (byte) 0x4B
    };

    byte[] key = {(byte) 4};
    
    // EXECUTION
    FeistelCipher feistelCipher = new FeistelCipher(
        _ -> roundKeys, feistelFunction, key);

    byte[] decryptedMessage = feistelCipher.cipher(cipherText, DECRYPT);

    // ASSERTION
    Assert.assertEquals(decryptedMessage, expectedMessage);
  }

  @Test
  void testFeistelCipherCycle() {
    // SETUP
    List<byte[]> roundKeys = List.of(
        new byte[] { (byte) 0x58, (byte) 0xE2 },
        new byte[] { (byte) 0x42, (byte) 0xE6 },
        new byte[] { (byte) 0x99, (byte) 0x26 },
        new byte[] { (byte) 0x44, (byte) 0xA4 },
        new byte[] { (byte) 0xFD, (byte) 0x50 },
        new byte[] { (byte) 0xDA, (byte) 0xAE },
        new byte[] { (byte) 0x75, (byte) 0x00 },
        new byte[] { (byte) 0x8D, (byte) 0xFB }
    );

    byte[] message = {
        (byte) 0x18, (byte) 0x7C, (byte) 0xAF, (byte) 0x99
    };

    byte[] key = {(byte) 4};

    // EXECUTION
    FeistelCipher feistelCipher = new FeistelCipher(
        _ -> roundKeys, feistelFunction, key);

    byte[] decryptedMessage = feistelCipher.cipher(feistelCipher.cipher(message, ENCRYPT), DECRYPT);

    // ASSERTION
    Assert.assertEquals(decryptedMessage, message);
  }
}
