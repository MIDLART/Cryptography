package testsCW;

import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.loki97.LOKI97;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import static org.client.crypto.enums.EncryptionMode.*;
import static org.client.crypto.enums.PackingMode.*;
import static org.client.crypto.loki97.boxes.Sboxes.initS1;
import static org.client.crypto.loki97.boxes.Sboxes.initS2;

public class LOKI97SymmetricAlgorithmTest {

  String testDirectory = "src/test/resources/CWTestsLOKI97";

  @BeforeClass
  void initTestClass() throws IOException {
    if (Files.notExists(Path.of(testDirectory))) {
      Files.createDirectory(Path.of(testDirectory));
    }
  }

  // region --- Test of cipher modes via both encryption and decryption ---

  @Test(dataProvider = "messageKeyBundle")
  void testECBCycle(byte[] key, byte[] message) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCBCCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testPCBCCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCFBCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testOFBCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCTRCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorDeltaBundle")
  void testRDCycle(byte[] key, byte[] message, byte[] initVector, BigInteger delta) {

    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(
        cryptoSystem, RandomDelta, ZEROS, initVector, delta);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  // endregion

  // region --- Test file encryption and decryption ---

  @Test
  void testTextFileCycle() throws IOException {
    // SETUP
    byte[] key = {
        (byte) 0xC8, (byte) 0x87, (byte) 0x58, (byte) 0x3B, (byte) 0x14, (byte) 0x65, (byte) 0xA7, (byte) 0xD5,
        (byte) 0x70, (byte) 0x2A, (byte) 0xFE, (byte) 0x0D, (byte) 0x2C, (byte) 0x99, (byte) 0x83, (byte) 0x04,
        (byte) 0x14, (byte) 0x2D, (byte) 0xC3, (byte) 0xDC, (byte) 0xE7, (byte) 0x06, (byte) 0x1E, (byte) 0x34,
        (byte) 0x58, (byte) 0xDA, (byte) 0x72, (byte) 0x9D, (byte) 0xBC, (byte) 0x31, (byte) 0xA5, (byte) 0x8A,
    };
    byte[] initVector = {
        (byte) 0x24, (byte) 0x76, (byte) 0x72, (byte) 0x7A, (byte) 0xC4, (byte) 0x03, (byte) 0xDC, (byte) 0x0F,
        (byte) 0x54, (byte) 0xF4, (byte) 0x82, (byte) 0x74, (byte) 0x72, (byte) 0xD6, (byte) 0x4A, (byte) 0x1F,
    };

    String in = testDirectory + "/allocator_red_black_tree_tests.cpp";
    String encOut = testDirectory + "/encryptedAllocRBTTests";
    String decOut = testDirectory + "/decryptedAllocRBTTests.cpp";

    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, PKCS7, initVector);

    cryptoContext.encrypt(in, encOut);
    cryptoContext.decrypt(encOut, decOut);

    // ASSERTION
    Assert.assertTrue(areFilesEqual(in, decOut));
  }

  @Test
  void testPictureFileCycle() throws IOException {
    // SETUP
    byte[] key = {
        (byte) 0x96, (byte) 0x3F, (byte) 0x67, (byte) 0x59, (byte) 0x62, (byte) 0xAB, (byte) 0xEB, (byte) 0x18,
        (byte) 0x4E, (byte) 0x79, (byte) 0x21, (byte) 0xF1, (byte) 0x37, (byte) 0x77, (byte) 0xFF, (byte) 0xC0,
        (byte) 0xE7, (byte) 0x06, (byte) 0xB2, (byte) 0x04, (byte) 0xF2, (byte) 0x83, (byte) 0xFB, (byte) 0x77,
    };
    byte[] initVector = {
        (byte) 0xC2, (byte) 0x5E, (byte) 0xFC, (byte) 0x27, (byte) 0x8D, (byte) 0xED, (byte) 0xB8, (byte) 0x35,
        (byte) 0xA3, (byte) 0x34, (byte) 0x80, (byte) 0x56, (byte) 0x23, (byte) 0x3A, (byte) 0x75, (byte) 0xE5,
    };

    String in = testDirectory + "/picture.jpg";
    String encOut = testDirectory + "/encryptedPicture";
    String decOut = testDirectory + "/decryptedPicture.jpg";

    // EXECUTION
    var s1 = initS1();
    var s2 = initS2();

    var cryptoSystem = new LOKI97(key, s1, s2);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ANSIX923, initVector);

    cryptoContext.encrypt(in, encOut);
    cryptoContext.decrypt(encOut, decOut);

    // ASSERTION
    Assert.assertTrue(areFilesEqual(in, decOut));
  }

  // endregion


  // region --- Data providers ---

  byte[][] messagesProvider() {
    return new byte[][] {
        {
            (byte) 0xFD, (byte) 0xCE, (byte) 0xA5, (byte) 0x8D,
            (byte) 0x37, (byte) 0x0B, (byte) 0x42, (byte) 0x50,
        },
        {
            (byte) 0x57, (byte) 0x30, (byte) 0x4A, (byte) 0xF5, (byte) 0x44, (byte) 0xA9, (byte) 0x55, (byte) 0x70,
            (byte) 0x4D, (byte) 0xA5, (byte) 0x39, (byte) 0x2F, (byte) 0xEF, (byte) 0x67, (byte) 0x12, (byte) 0x33,
            (byte) 0x7D, (byte) 0xF4, (byte) 0x42, (byte) 0xED, (byte) 0x32, (byte) 0xB4, (byte) 0x50, (byte) 0x84,
            (byte) 0x8D, (byte) 0x8D, (byte) 0xE6, (byte) 0x73, (byte) 0x69, (byte) 0x5F, (byte) 0x6F, (byte) 0x91,
            (byte) 0x5E, (byte) 0x60, (byte) 0x55, (byte) 0x73, (byte) 0xC0, (byte) 0xF2, (byte) 0x99, (byte) 0xA5,
            (byte) 0xFE, (byte) 0xE6, (byte) 0x35, (byte) 0x17, (byte) 0x99, (byte) 0x59, (byte) 0x98, (byte) 0x54,
            (byte) 0xD4, (byte) 0x1B, (byte) 0xE8, (byte) 0x98, (byte) 0x21, (byte) 0xA4, (byte) 0xBE, (byte) 0x9B,
            (byte) 0xEE, (byte) 0xC5, (byte) 0xBC, (byte) 0x7D, (byte) 0x43, (byte) 0x6B, (byte) 0x4B, (byte) 0xD6,
            (byte) 0x17, (byte) 0xF3, (byte) 0x67, (byte) 0xF3, (byte) 0xF7, (byte) 0x11, (byte) 0x53, (byte) 0x7B,
            (byte) 0xB3, (byte) 0x65, (byte) 0x3D, (byte) 0xCD, (byte) 0xC5, (byte) 0xB4, (byte) 0xB0, (byte) 0xDD,
            (byte) 0xA8, (byte) 0x69, (byte) 0xBC, (byte) 0xD8, (byte) 0xBD, (byte) 0x4E, (byte) 0x64, (byte) 0xC2,
            (byte) 0x00, (byte) 0x75, (byte) 0x0D, (byte) 0x20, (byte) 0x91, (byte) 0x4C, (byte) 0xC4, (byte) 0x89,
            (byte) 0x8D, (byte) 0x7F, (byte) 0x44, (byte) 0x1B, (byte) 0xB3, (byte) 0x5A, (byte) 0x90, (byte) 0xF3,
            (byte) 0xEB, (byte) 0xBD, (byte) 0xDD, (byte) 0xE3, (byte) 0x91, (byte) 0xE0, (byte) 0x64, (byte) 0xC3,
            (byte) 0x44, (byte) 0xE4, (byte) 0x17, (byte) 0x2A, (byte) 0xAC, (byte) 0xC5, (byte) 0xBA, (byte) 0x09,
            (byte) 0xB7, (byte) 0xBA, (byte) 0xD4, (byte) 0xC7, (byte) 0x94, (byte) 0x7F, (byte) 0xEE, (byte) 0x6C,
        },
        {
            (byte) 0x59, (byte) 0x19, (byte) 0x40, (byte) 0x73, (byte) 0xAC, (byte) 0x50, (byte) 0xBE, (byte) 0xEE,
            (byte) 0x7C, (byte) 0x24
        },
        {
            (byte) 0xF5, (byte) 0x01, (byte) 0x77, (byte) 0x6E, (byte) 0x14, (byte) 0x41, (byte) 0x2C, (byte) 0x74,
            (byte) 0xE4, (byte) 0x47, (byte) 0xA0, (byte) 0x01, (byte) 0x1C, (byte) 0x96, (byte) 0x6C, (byte) 0x4A,
            (byte) 0x97, (byte) 0xBF, (byte) 0x78, (byte) 0xAA, (byte) 0x72, (byte) 0x06, (byte) 0x82, (byte) 0xAE,
            (byte) 0x15, (byte) 0x9E, (byte) 0xAD, (byte) 0x2C, (byte) 0x3F, (byte) 0xF7, (byte) 0x46, (byte) 0x71,
            (byte) 0x5E, (byte) 0x33, (byte) 0x85, (byte) 0xF8, (byte) 0x5A, (byte) 0x44, (byte) 0xFC, (byte) 0x71,
            (byte) 0x04, (byte) 0xDF, (byte) 0x50, (byte) 0x3C, (byte) 0x82, (byte) 0x23, (byte) 0x58, (byte) 0xCC,
            (byte) 0x69, (byte) 0xBC, (byte) 0xFD, (byte) 0xF4, (byte) 0xA3, (byte) 0xEA, (byte) 0x93, (byte) 0xA8,
            (byte) 0x5A, (byte) 0xCD, (byte) 0x07, (byte) 0xBA, (byte) 0xAA, (byte) 0xD1, (byte) 0xDA, (byte) 0xCD,
            (byte) 0x16, (byte) 0x89, (byte) 0x01, (byte) 0x6C, (byte) 0x9A, (byte) 0x28, (byte) 0x2D, (byte) 0x61,
            (byte) 0x17, (byte) 0x09, (byte) 0x1F, (byte) 0xE7, (byte) 0x02, (byte) 0x1E, (byte) 0xAB, (byte) 0xD0,
            (byte) 0x22, (byte) 0x49, (byte) 0x8D, (byte) 0x29, (byte) 0x0D, (byte) 0xBA, (byte) 0x5C, (byte) 0xD2,
            (byte) 0x0A, (byte) 0x35, (byte) 0xDD, (byte) 0xDB, (byte) 0xC8, (byte) 0x38, (byte) 0xFA, (byte) 0x89,
            (byte) 0x86, (byte) 0xA8, (byte) 0x5C, (byte) 0x6C, (byte) 0xF3, (byte) 0x5A, (byte) 0xA8, (byte) 0x5E,
            (byte) 0x08, (byte) 0x43, (byte) 0x19
        }
    };
  }

  byte[][] keysProvider() {
    return new byte[][] {
        {
            (byte) 0x96, (byte) 0xD9, (byte) 0xA2, (byte) 0xAF, (byte) 0xE6, (byte) 0x7F, (byte) 0xA6, (byte) 0xCB,
            (byte) 0x0A, (byte) 0x42, (byte) 0xF7, (byte) 0xC2, (byte) 0x68, (byte) 0xBC, (byte) 0xC8, (byte) 0xF6,
        },
        {
            (byte) 0xD9, (byte) 0x6F, (byte) 0xFF, (byte) 0x5F, (byte) 0xA7, (byte) 0x51, (byte) 0xC2, (byte) 0xB3,
            (byte) 0x7F, (byte) 0x3D, (byte) 0xB9, (byte) 0x8B, (byte) 0xA4, (byte) 0x9E, (byte) 0x13, (byte) 0x32,
        },
        {
            (byte) 0x30, (byte) 0x35, (byte) 0x34, (byte) 0x1F, (byte) 0x56, (byte) 0xBD, (byte) 0x06, (byte) 0xFF,
            (byte) 0xEA, (byte) 0x04, (byte) 0x5F, (byte) 0x63, (byte) 0x01, (byte) 0xB3, (byte) 0xEA, (byte) 0x5C,
        },
        {
            (byte) 0xF0, (byte) 0x16, (byte) 0x98, (byte) 0x39, (byte) 0xF1, (byte) 0x3D, (byte) 0x27, (byte) 0x6D,
            (byte) 0x5E, (byte) 0xA3, (byte) 0x49, (byte) 0x03, (byte) 0xD5, (byte) 0xAB, (byte) 0x17, (byte) 0xAB,
            (byte) 0xCB, (byte) 0x3A, (byte) 0xE2, (byte) 0x75, (byte) 0x91, (byte) 0x99, (byte) 0xEF, (byte) 0x59,
        },
        {
            (byte) 0x7A, (byte) 0xEE, (byte) 0x3A, (byte) 0xDA, (byte) 0xB1, (byte) 0x81, (byte) 0x33, (byte) 0x84,
            (byte) 0x2B, (byte) 0xCE, (byte) 0x75, (byte) 0x31, (byte) 0x96, (byte) 0x17, (byte) 0x0D, (byte) 0xC4,
            (byte) 0xB2, (byte) 0x81, (byte) 0xEB, (byte) 0x48, (byte) 0x9E, (byte) 0x02, (byte) 0x8C, (byte) 0xE7,
        },
        {
            (byte) 0x96, (byte) 0x3F, (byte) 0x67, (byte) 0x59, (byte) 0x62, (byte) 0xAB, (byte) 0xEB, (byte) 0x18,
            (byte) 0x4E, (byte) 0x79, (byte) 0x21, (byte) 0xF1, (byte) 0x37, (byte) 0x77, (byte) 0xFF, (byte) 0xC0,
            (byte) 0xE7, (byte) 0x06, (byte) 0xB2, (byte) 0x04, (byte) 0xF2, (byte) 0x83, (byte) 0xFB, (byte) 0x77,
        },
        {
            (byte) 0xA8, (byte) 0x9E, (byte) 0x2A, (byte) 0x5B, (byte) 0x69, (byte) 0x20, (byte) 0xB7, (byte) 0xDD,
            (byte) 0xD5, (byte) 0x94, (byte) 0x30, (byte) 0x73, (byte) 0x43, (byte) 0x8B, (byte) 0x7B, (byte) 0x52,
            (byte) 0xFB, (byte) 0xD7, (byte) 0x00, (byte) 0xF9, (byte) 0x50, (byte) 0xB6, (byte) 0xFC, (byte) 0xD1,
            (byte) 0x9B, (byte) 0xC9, (byte) 0x8B, (byte) 0xFC, (byte) 0x4B, (byte) 0x5E, (byte) 0x97, (byte) 0xAC,
        },
        {
            (byte) 0x08, (byte) 0xF4, (byte) 0x5B, (byte) 0xCD, (byte) 0x62, (byte) 0x4E, (byte) 0xFE, (byte) 0x0C,
            (byte) 0xDF, (byte) 0xE3, (byte) 0x05, (byte) 0x3D, (byte) 0x58, (byte) 0x18, (byte) 0x1B, (byte) 0x7B,
            (byte) 0xB9, (byte) 0x1B, (byte) 0xD4, (byte) 0x72, (byte) 0x33, (byte) 0xF9, (byte) 0x9E, (byte) 0x6D,
            (byte) 0xBB, (byte) 0xC5, (byte) 0x9A, (byte) 0x23, (byte) 0x88, (byte) 0xF0, (byte) 0x48, (byte) 0x64,
        },
        {
            (byte) 0xC8, (byte) 0x87, (byte) 0x58, (byte) 0x3B, (byte) 0x14, (byte) 0x65, (byte) 0xA7, (byte) 0xD5,
            (byte) 0x70, (byte) 0x2A, (byte) 0xFE, (byte) 0x0D, (byte) 0x2C, (byte) 0x99, (byte) 0x83, (byte) 0x04,
            (byte) 0x14, (byte) 0x2D, (byte) 0xC3, (byte) 0xDC, (byte) 0xE7, (byte) 0x06, (byte) 0x1E, (byte) 0x34,
            (byte) 0x58, (byte) 0xDA, (byte) 0x72, (byte) 0x9D, (byte) 0xBC, (byte) 0x31, (byte) 0xA5, (byte) 0x8A,
        }
    };
  }

  BigInteger[] deltasProvider() {
    return new BigInteger[] {
        new BigInteger("0CB803D5AB15D23E", 16),
        new BigInteger("8837E85E8F4DD256", 16),
        new BigInteger("ECA2FA29F1AB2AF0", 16),
        new BigInteger("FFFFFFFFFFFFFFFF", 16),
        new BigInteger("F000000000000000", 16),
    };
  }

  byte[][] initVectorsProvider() {
    return new byte[][] {
        {
          (byte) 0x89, (byte) 0x01, (byte) 0x37, (byte) 0x23, (byte) 0xA0, (byte) 0xB1, (byte) 0x99, (byte) 0xE4,
          (byte) 0xDE, (byte) 0x73, (byte) 0x23, (byte) 0x5A, (byte) 0x5B, (byte) 0x52, (byte) 0x8F, (byte) 0x8B,
        },
        {
          (byte) 0xB3, (byte) 0x58, (byte) 0x98, (byte) 0x6D, (byte) 0xA4, (byte) 0xAB, (byte) 0xD7, (byte) 0x5C,
          (byte) 0x13, (byte) 0x48, (byte) 0x67, (byte) 0x68, (byte) 0x34, (byte) 0x90, (byte) 0x53, (byte) 0xD4,
        },
        {
          (byte) 0x9C, (byte) 0xBD, (byte) 0xE5, (byte) 0x4F, (byte) 0x3F, (byte) 0xC6, (byte) 0x14, (byte) 0x87,
          (byte) 0xF9, (byte) 0xDB, (byte) 0xB2, (byte) 0x46, (byte) 0x14, (byte) 0xEB, (byte) 0xEA, (byte) 0x48,
        },
        {
          (byte) 0x25, (byte) 0xA1, (byte) 0x14, (byte) 0x82, (byte) 0x13, (byte) 0x28, (byte) 0xC2, (byte) 0x06,
          (byte) 0x4D, (byte) 0x0C, (byte) 0x60, (byte) 0x18, (byte) 0x0B, (byte) 0x9D, (byte) 0x77, (byte) 0x37,
        }
    };
  }

  @DataProvider(name = "messageKeyBundle")
  Object[][] messageKeyProvider() {
    byte[][] keys = keysProvider();
    byte[][] messages = messagesProvider();

    int resLen = keys.length * messages.length;
    byte[][][] res = new byte[resLen][][];

    for (int i = 0; i < keys.length; ++i) {
      for (int j = 0; j < messages.length; ++j) {
        int ind = i * messages.length + j;
        res[ind] = new byte[][] { keys[i], messages[j] };
      }
    }

    return res;
  }

  @DataProvider(name = "messageKeyInitVectorBundle")
  Object[][] messageKeyInitVectorProvider() {
    byte[][] keys = keysProvider();
    byte[][] messages = messagesProvider();
    byte[][] initVectors = initVectorsProvider();

    int resLen = keys.length * messages.length * initVectors.length;
    byte[][][] res = new byte[resLen][][];

    for (int i = 0; i < keys.length; ++i) {
      for (int j = 0; j < messages.length; ++j) {
        for (int k = 0; k < initVectors.length; ++k) {
          int ind = i * messages.length * initVectors.length + j * initVectors.length + k;
          res[ind] = new byte[][] { keys[i], messages[j], initVectors[k] };
        }
      }
    }

    return res;
  }

  @DataProvider(name = "messageKeyInitVectorDeltaBundle")
  Object[][] messageKeyInitVectorDeltaProvider() {
    byte[][] keys = keysProvider();
    byte[][] messages = messagesProvider();
    byte[][] initVectors = initVectorsProvider();
    BigInteger[] deltas = deltasProvider();

    int resLen = keys.length * messages.length * initVectors.length * deltas.length;
    Object[][] res = new Object[resLen][];

    for (int i = 0; i < keys.length; ++i) {
      for (int j = 0; j < messages.length; ++j) {
        for (int k = 0; k < initVectors.length; ++k) {
          for (int l = 0; l < deltas.length; ++l)
          {
            int ind = i * messages.length * initVectors.length * deltas.length +
                    j * initVectors.length * deltas.length + k * deltas.length + l;
            res[ind] = new Object[] { keys[i], messages[j], initVectors[k], deltas[l] };
          }
        }
      }
    }

    return res;
  }

  // endregion

  // region -- Utility --

  boolean areFilesEqual(String path1, String path2) throws IOException {
    try (FileChannel fileChannel1 = FileChannel.open(Path.of(path1), StandardOpenOption.READ);
         FileChannel fileChannel2 = FileChannel.open(Path.of(path2), StandardOpenOption.READ)) {
      if (fileChannel1.size() != fileChannel2.size()) {
        return false;
      }

      int blockSize = 1 << 16;
      byte[] arr1 = new byte[blockSize];
      byte[] arr2 = new byte[blockSize];
      for (long i = 0; i < fileChannel1.size(); i += blockSize) {
        fileChannel1.read(ByteBuffer.wrap(arr1));
        fileChannel2.read(ByteBuffer.wrap(arr2));
        if (!Arrays.equals(arr1, arr2)) {
          return false;
        }
      }

      return true;
    }
  }

  // endregion
}