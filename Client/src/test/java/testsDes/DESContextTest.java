package testsDes;

import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.des.DES;
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
import java.util.*;

import static org.client.crypto.enums.EncryptionMode.*;
import static org.client.crypto.enums.PackingMode.*;

public class DESContextTest {

  String testDirectory = "src/test/resources/DESContextTest";

  @BeforeClass
  void initTestClass() throws IOException {
    if (Files.notExists(Path.of(testDirectory))) {
      Files.createDirectory(Path.of(testDirectory));
    }
  }

  // region --- Test of cipher modes via separate encryption and decryption without using padding ---

  @Test
  void testECBEncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x25, (byte) 0xB9, (byte) 0x67, (byte) 0xD8, (byte) 0x7A, (byte) 0x97, (byte) 0x00, (byte) 0x30,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);
    
    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testECBDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x25, (byte) 0xB9, (byte) 0x67, (byte) 0xD8, (byte) 0x7A, (byte) 0x97, (byte) 0x00, (byte) 0x30,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testCBCEncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0xCE, (byte) 0xDC, (byte) 0xDB, (byte) 0x52, (byte) 0xDF, (byte) 0x16, (byte) 0x22, (byte) 0xAD,
        (byte) 0x60, (byte) 0xB0, (byte) 0x17, (byte) 0xBB, (byte) 0x29, (byte) 0xF9, (byte) 0xFD, (byte) 0xEF,
        (byte) 0x85, (byte) 0xAA, (byte) 0x8A, (byte) 0xD4, (byte) 0x6B, (byte) 0x1B, (byte) 0x15, (byte) 0xA5,
        (byte) 0x34, (byte) 0xFA, (byte) 0x97, (byte) 0x16, (byte) 0xB6, (byte) 0x02, (byte) 0xEB, (byte) 0xC5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, NO, initVector);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testCBCDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0xCE, (byte) 0xDC, (byte) 0xDB, (byte) 0x52, (byte) 0xDF, (byte) 0x16, (byte) 0x22, (byte) 0xAD,
        (byte) 0x60, (byte) 0xB0, (byte) 0x17, (byte) 0xBB, (byte) 0x29, (byte) 0xF9, (byte) 0xFD, (byte) 0xEF,
        (byte) 0x85, (byte) 0xAA, (byte) 0x8A, (byte) 0xD4, (byte) 0x6B, (byte) 0x1B, (byte) 0x15, (byte) 0xA5,
        (byte) 0x34, (byte) 0xFA, (byte) 0x97, (byte) 0x16, (byte) 0xB6, (byte) 0x02, (byte) 0xEB, (byte) 0xC5,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, NO, initVector);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testPCBCEncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0xCE, (byte) 0xDC, (byte) 0xDB, (byte) 0x52, (byte) 0xDF, (byte) 0x16, (byte) 0x22, (byte) 0xAD,
        (byte) 0xEE, (byte) 0xAE, (byte) 0xAE, (byte) 0x58, (byte) 0xAE, (byte) 0xE9, (byte) 0x9D, (byte) 0x1C,
        (byte) 0x94, (byte) 0x1E, (byte) 0xBD, (byte) 0xFF, (byte) 0x35, (byte) 0xA8, (byte) 0x6C, (byte) 0x7D,
        (byte) 0xB0, (byte) 0x7B, (byte) 0x66, (byte) 0x12, (byte) 0x9E, (byte) 0xCA, (byte) 0x55, (byte) 0xA2,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, NO, initVector);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testPCBCDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0xCE, (byte) 0xDC, (byte) 0xDB, (byte) 0x52, (byte) 0xDF, (byte) 0x16, (byte) 0x22, (byte) 0xAD,
        (byte) 0xEE, (byte) 0xAE, (byte) 0xAE, (byte) 0x58, (byte) 0xAE, (byte) 0xE9, (byte) 0x9D, (byte) 0x1C,
        (byte) 0x94, (byte) 0x1E, (byte) 0xBD, (byte) 0xFF, (byte) 0x35, (byte) 0xA8, (byte) 0x6C, (byte) 0x7D,
        (byte) 0xB0, (byte) 0x7B, (byte) 0x66, (byte) 0x12, (byte) 0x9E, (byte) 0xCA, (byte) 0x55, (byte) 0xA2,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext =  new SymmetricAlgorithm(cryptoSystem, PCBC, NO, initVector);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testCFBEncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0xB2, (byte) 0xE9, (byte) 0x0D, (byte) 0xE3, (byte) 0x86, (byte) 0xAB, (byte) 0x85, (byte) 0x37,
        (byte) 0x87, (byte) 0x91, (byte) 0xAF, (byte) 0xFF, (byte) 0xD2, (byte) 0x68, (byte) 0x97, (byte) 0x6B,
        (byte) 0x3A, (byte) 0x21, (byte) 0x91, (byte) 0x5D, (byte) 0xBD, (byte) 0x27, (byte) 0x28, (byte) 0x1F,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, NO, initVector);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testCFBDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0xB2, (byte) 0xE9, (byte) 0x0D, (byte) 0xE3, (byte) 0x86, (byte) 0xAB, (byte) 0x85, (byte) 0x37,
        (byte) 0x87, (byte) 0x91, (byte) 0xAF, (byte) 0xFF, (byte) 0xD2, (byte) 0x68, (byte) 0x97, (byte) 0x6B,
        (byte) 0x3A, (byte) 0x21, (byte) 0x91, (byte) 0x5D, (byte) 0xBD, (byte) 0x27, (byte) 0x28, (byte) 0x1F,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, NO, initVector);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testOFBEncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0x88, (byte) 0xF4, (byte) 0x43, (byte) 0x84, (byte) 0xCD, (byte) 0x28, (byte) 0x4F, (byte) 0x53,
        (byte) 0xCD, (byte) 0xAB, (byte) 0x87, (byte) 0xDD, (byte) 0xE2, (byte) 0xFF, (byte) 0xB0, (byte) 0x8D,
        (byte) 0xDA, (byte) 0xA5, (byte) 0x34, (byte) 0x8C, (byte) 0x3B, (byte) 0xAB, (byte) 0x21, (byte) 0xBC,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, NO, initVector);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testOFBDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0x88, (byte) 0xF4, (byte) 0x43, (byte) 0x84, (byte) 0xCD, (byte) 0x28, (byte) 0x4F, (byte) 0x53,
        (byte) 0xCD, (byte) 0xAB, (byte) 0x87, (byte) 0xDD, (byte) 0xE2, (byte) 0xFF, (byte) 0xB0, (byte) 0x8D,
        (byte) 0xDA, (byte) 0xA5, (byte) 0x34, (byte) 0x8C, (byte) 0x3B, (byte) 0xAB, (byte) 0x21, (byte) 0xBC,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, NO, initVector);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testCTREncryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0x27, (byte) 0x96, (byte) 0xB8, (byte) 0x03, (byte) 0x4A, (byte) 0x46, (byte) 0xC6, (byte) 0x01,
        (byte) 0xA6, (byte) 0x03, (byte) 0x41, (byte) 0xB7, (byte) 0x5E, (byte) 0x47, (byte) 0xA4, (byte) 0xD5,
        (byte) 0xA1, (byte) 0x1E, (byte) 0x44, (byte) 0x5D, (byte) 0x96, (byte) 0xE3, (byte) 0xF4, (byte) 0x47,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, NO, initVector);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testCTRDecryptionWithoutPadding() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0x27, (byte) 0x96, (byte) 0xB8, (byte) 0x03, (byte) 0x4A, (byte) 0x46, (byte) 0xC6, (byte) 0x01,
        (byte) 0xA6, (byte) 0x03, (byte) 0x41, (byte) 0xB7, (byte) 0x5E, (byte) 0x47, (byte) 0xA4, (byte) 0xD5,
        (byte) 0xA1, (byte) 0x1E, (byte) 0x44, (byte) 0x5D, (byte) 0x96, (byte) 0xE3, (byte) 0xF4, (byte) 0x47,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, NO, initVector);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testRDEncryptionWithoutPadding() {
    // SETUP
    BigInteger delta = new BigInteger("89ED605002AA5152", 16);

    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };
    byte[] expectedCipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0xC3, (byte) 0xE4, (byte) 0x2B, (byte) 0xE9, (byte) 0xC8, (byte) 0x9D, (byte) 0x7A, (byte) 0xAA,
        (byte) 0xBE, (byte) 0x23, (byte) 0x9F, (byte) 0x60, (byte) 0x89, (byte) 0x07, (byte) 0xC1, (byte) 0x3D,
        (byte) 0xB6, (byte) 0x4A, (byte) 0xF3, (byte) 0x1A, (byte) 0x4E, (byte) 0xCF, (byte) 0xBD, (byte) 0xD3,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(
        cryptoSystem, RandomDelta, NO, initVector, delta);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testRDDecryptionWithoutPadding() {
    // SETUP
    BigInteger delta = new BigInteger("89ED605002AA5152", 16);

    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x40, (byte) 0x32, (byte) 0xAD, (byte) 0x25, (byte) 0xA0, (byte) 0xEA, (byte) 0x0C, (byte) 0x8B,
    };
    byte[] cipher = {
        (byte) 0x0E, (byte) 0x78, (byte) 0x19, (byte) 0xF1, (byte) 0x55, (byte) 0x96, (byte) 0x44, (byte) 0x6B,
        (byte) 0xC3, (byte) 0xE4, (byte) 0x2B, (byte) 0xE9, (byte) 0xC8, (byte) 0x9D, (byte) 0x7A, (byte) 0xAA,
        (byte) 0xBE, (byte) 0x23, (byte) 0x9F, (byte) 0x60, (byte) 0x89, (byte) 0x07, (byte) 0xC1, (byte) 0x3D,
        (byte) 0xB6, (byte) 0x4A, (byte) 0xF3, (byte) 0x1A, (byte) 0x4E, (byte) 0xCF, (byte) 0xBD, (byte) 0xD3,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3, (byte) 0xD5,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(
        cryptoSystem, RandomDelta, NO, initVector, delta);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  // endregion

  // region --- Test padding with separate encryption and decryption  ---

  @Test
  void testZerosOneBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xB8, (byte) 0xA5, (byte) 0xA6, (byte) 0x83, (byte) 0x17, (byte) 0xFC, (byte) 0x50, (byte) 0xC6,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testZerosOneBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xB8, (byte) 0xA5, (byte) 0xA6, (byte) 0x83, (byte) 0x17, (byte) 0xFC, (byte) 0x50, (byte) 0xC6,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testZerosSevenBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xC6, (byte) 0x39, (byte) 0x2F, (byte) 0x5D, (byte) 0x8D, (byte) 0x41, (byte) 0xA0, (byte) 0x67,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testZerosSevenBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xC6, (byte) 0x39, (byte) 0x2F, (byte) 0x5D, (byte) 0x8D, (byte) 0x41, (byte) 0xA0, (byte) 0x67,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testZerosEightBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x7E, (byte) 0x95, (byte) 0xE8, (byte) 0x3E, (byte) 0x96, (byte) 0x17, (byte) 0x42, (byte) 0x21,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testZerosEightBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x7E, (byte) 0x95, (byte) 0xE8, (byte) 0x3E, (byte) 0x96, (byte) 0x17, (byte) 0x42, (byte) 0x21,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testZerosPaddingLineAffectionWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x95, (byte) 0xE8, (byte) 0x3E, (byte) 0x96, (byte) 0x17, (byte) 0x42, (byte) 0x21,
        (byte) 0xC6, (byte) 0x39, (byte) 0x2F, (byte) 0x5D, (byte) 0x8D, (byte) 0x41, (byte) 0xA0, (byte) 0x67,
    };
    byte[] expectedMessage = {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testZerosPaddingFailureWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    byte[] expectedDecryptedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] actualDecryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    Assert.assertEquals(actualDecryptedMessage, expectedDecryptedMessage);
  }


  @Test
  void testANSIX923sOneBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xE0, (byte) 0x6E, (byte) 0x53, (byte) 0x07, (byte) 0x48, (byte) 0x36, (byte) 0x9E, (byte) 0xCF,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testANSIX923sOneBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xE0, (byte) 0x6E, (byte) 0x53, (byte) 0x07, (byte) 0x48, (byte) 0x36, (byte) 0x9E, (byte) 0xCF,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testANSIX923SevenBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xD4, (byte) 0x18, (byte) 0xFB, (byte) 0xB1, (byte) 0x18, (byte) 0x83, (byte) 0x4B, (byte) 0x92,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testANSIX923SevenBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xD4, (byte) 0x18, (byte) 0xFB, (byte) 0xB1, (byte) 0x18, (byte) 0x83, (byte) 0x4B, (byte) 0x92,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testANSIX923EightBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xCF, (byte) 0x1F, (byte) 0x55, (byte) 0xAF, (byte) 0xD6, (byte) 0x88, (byte) 0x89, (byte) 0x02,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testANSIX923EightBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xCF, (byte) 0x1F, (byte) 0x55, (byte) 0xAF, (byte) 0xD6, (byte) 0x88, (byte) 0x89, (byte) 0x02,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testANSIX923PaddingLineAffectionWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0xED, (byte) 0x67, (byte) 0x80, (byte) 0xBF, (byte) 0x28, (byte) 0x84, (byte) 0x34, (byte) 0xFF,
        (byte) 0xD4, (byte) 0x18, (byte) 0xFB, (byte) 0xB1, (byte) 0x18, (byte) 0x83, (byte) 0x4B, (byte) 0x92,
    };
    byte[] expectedMessage = {
        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ANSIX923);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }


  @Test
  void testPKCS7OneBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xE0, (byte) 0x6E, (byte) 0x53, (byte) 0x07, (byte) 0x48, (byte) 0x36, (byte) 0x9E, (byte) 0xCF,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testPKCS7OneBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xE0, (byte) 0x6E, (byte) 0x53, (byte) 0x07, (byte) 0x48, (byte) 0x36, (byte) 0x9E, (byte) 0xCF,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testPKCS7SevenBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xD6, (byte) 0xF7, (byte) 0x62, (byte) 0x5D, (byte) 0xB7, (byte) 0x78, (byte) 0x90, (byte) 0xDA
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testPKCS7SevenBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xF3, (byte) 0x4D, (byte) 0x4E, (byte) 0xFA, (byte) 0x06, (byte) 0xFF, (byte) 0xF1, (byte) 0x5C,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testPKCS7EightBytePaddingWithECBEncryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };
    byte[] expectedCipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x14, (byte) 0x02, (byte) 0x93, (byte) 0x89, (byte) 0xD2, (byte) 0x89, (byte) 0x89, (byte) 0xE3
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualCipher = cryptoContext.encrypt(message);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testPKCS7EightBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xB6, (byte) 0xD8, (byte) 0x28, (byte) 0x59, (byte) 0xBE, (byte) 0x26, (byte) 0x78, (byte) 0xA7,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testPKCS7PaddingLineAffectionWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x6A, (byte) 0xB9, (byte) 0x6A, (byte) 0xAA, (byte) 0xEA, (byte) 0x35, (byte) 0xBA, (byte) 0xF5,
        (byte) 0xF3, (byte) 0x4D, (byte) 0x4E, (byte) 0xFA, (byte) 0x06, (byte) 0xFF, (byte) 0xF1, (byte) 0x5C,
    };
    byte[] expectedMessage = {
        (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x05,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, PKCS7);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }


  @Test
  void testISO10126OneBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xE0, (byte) 0x6E, (byte) 0x53, (byte) 0x07, (byte) 0x48, (byte) 0x36, (byte) 0x9E, (byte) 0xCF,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11, (byte) 0x8B, (byte) 0x12, (byte) 0x0B, (byte) 0x12, (byte) 0x80, (byte) 0xF3,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ISO10126);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testISO10126SevenBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = { // cipher of random
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0x90, (byte) 0xAD, (byte) 0x96, (byte) 0x56, (byte) 0x8F, (byte) 0x58, (byte) 0x71, (byte) 0xA1,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ISO10126);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testISO10126EightBytePaddingWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = { // cipher of random
        (byte) 0x7E, (byte) 0x15, (byte) 0x37, (byte) 0x6B, (byte) 0x30, (byte) 0xBC, (byte) 0x33, (byte) 0x88,
        (byte) 0xA8, (byte) 0x7B, (byte) 0x8F, (byte) 0xDC, (byte) 0xD0, (byte) 0x95, (byte) 0x6C, (byte) 0x1F,
    };
    byte[] expectedMessage = {
        (byte) 0xAD, (byte) 0x5B, (byte) 0x20, (byte) 0x0E, (byte) 0xC8, (byte) 0x96, (byte) 0x6D, (byte) 0xD0,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ISO10126);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testISO10126PaddingLineAffectionWithECBDecryption() {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = { // cipher of random
        (byte) 0x6A, (byte) 0xB9, (byte) 0x6A, (byte) 0xAA, (byte) 0xEA, (byte) 0x35, (byte) 0xBA, (byte) 0xF5,
        (byte) 0x90, (byte) 0xAD, (byte) 0x96, (byte) 0x56, (byte) 0x8F, (byte) 0x58, (byte) 0x71, (byte) 0xA1,
    };
    byte[] expectedMessage = {
        (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x06, (byte) 0x05,
        (byte) 0x11,
    };

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ISO10126);

    byte[] actualMessage = cryptoContext.decrypt(cipher);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  // endregion

  // region --- Test of cipher modes via both encryption and decryption ---

  @Test(dataProvider = "messageKeyBundle")
  void testECBCycle(byte[] key, byte[] message) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCBCCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testPCBCCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCFBCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testOFBCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorBundle")
  void testCTRCycle(byte[] key, byte[] message, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  @Test(dataProvider = "messageKeyInitVectorDeltaBundle")
  void testRDCycle(byte[] key, byte[] message, byte[] initVector, BigInteger delta) {
    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(
        cryptoSystem, RandomDelta, ZEROS, initVector, delta);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    Assert.assertEquals(message, decryptedMessage);
  }

  // endregion

  // region --- Test file encryption and decryption ---

  @Test
  void testFileReadEncryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };
    byte[] expectedCipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };

    String in = testDirectory + "/wEncTestIn";
    writeToFile(in, message);
    String out = testDirectory + "/wEncTestInRes";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.encrypt(in, out);
    byte[] actualCipher = readFromFile(out);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testFileWriteEncryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };
    byte[] expectedCipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };

    String in = testDirectory + "/wEncTestOutMes";
    writeToFile(in, message);

    String out = testDirectory + "/wEncTestOut";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.encrypt(in, out);
    byte[] actualCipher = readFromFile(out);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testFileReadWriteEncryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] message = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };
    byte[] expectedCipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };

    String in = testDirectory + "/rwEncTestInC";
    String out = testDirectory + "/rwEncTestOutC";
    writeToFile(in, message);

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.encrypt(in, out);
    byte[] actualCipher = readFromFile(out);

    // ASSERTION
    Assert.assertEquals(actualCipher, expectedCipher);
  }

  @Test
  void testFileReadDecryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };

    String in = testDirectory + "/wDecTestIn";
    writeToFile(in, cipher);

    String out = testDirectory + "/wDecTestOut";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.decrypt(in, out);
    byte[] actualMessage = readFromFile(out);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testFileWriteDecryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };

    String in = testDirectory + "/wDecTestIn1";
    writeToFile(in, cipher);

    String out = testDirectory + "/wDecTestOut1";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.decrypt(in, out);
    byte[] actualMessage = readFromFile(out);

        // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }

  @Test
  void testFileReadWriteDecryptionWithECB() throws IOException {
    // SETUP
    byte[] key = { // 64-bit version: 39C3BB89D5E167D3
        (byte) 0x39, (byte) 0x86, (byte) 0xEC, (byte) 0x4D, (byte) 0x5C, (byte) 0x19, (byte) 0xE9
    };
    byte[] cipher = {
        (byte) 0x35, (byte) 0xFE, (byte) 0x36, (byte) 0xD3, (byte) 0xE3, (byte) 0x58, (byte) 0x5D, (byte) 0xEF,
        (byte) 0xBB, (byte) 0x4F, (byte) 0x73, (byte) 0xDF, (byte) 0x81, (byte) 0x7E, (byte) 0xE3, (byte) 0xAD,
    };
    byte[] expectedMessage = {
        (byte) 0x1A, (byte) 0xFC, (byte) 0x29, (byte) 0x57, (byte) 0x42, (byte) 0xCA, (byte) 0x50, (byte) 0x88,
        (byte) 0x40, (byte) 0xA4, (byte) 0xAA, (byte) 0xFA, (byte) 0x19, (byte) 0x8D, (byte) 0x7E, (byte) 0xAC,
    };

    String in = testDirectory + "/wDecTestIn";
    String out = testDirectory + "/wDecTestOut";
    writeToFile(in, cipher);

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, NO);

    cryptoContext.decrypt(in, out);
    byte[] actualMessage = readFromFile(out);

    // ASSERTION
    Assert.assertEquals(actualMessage, expectedMessage);
  }


  @Test
  void testTextFileCycle() throws IOException {
    // SETUP
    byte[] key = {
        (byte) 0x55, (byte) 0x46, (byte) 0xEA, (byte) 0xDD, (byte) 0x5C, (byte) 0x59, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x5a, (byte) 0xa8, (byte) 0x08, (byte) 0x43, (byte) 0x19, (byte) 0xe5, (byte) 0xd8, (byte) 0x2d,
    };

    String in = testDirectory + "/allocator_red_black_tree_tests.cpp";
    String encOut = testDirectory + "/encryptedAllocRBTTests";
    String decOut = testDirectory + "/decryptedAllocRBTTests";

    // EXECUTION
    var cryptoSystem = new DES(key);
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
        (byte) 0x55, (byte) 0x46, (byte) 0xEA, (byte) 0xDD, (byte) 0x5C, (byte) 0x59, (byte) 0xE9
    };
    byte[] initVector = {
        (byte) 0x5a, (byte) 0xa8, (byte) 0x08, (byte) 0x43, (byte) 0x19, (byte) 0xe5, (byte) 0xd8, (byte) 0x2d,
    };
    
    String in = testDirectory + "/picture.jpg";
    String encOut = testDirectory + "/encryptedPicture";
    String decOut = testDirectory + "/decryptedPicture.jpg";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, ANSIX923, initVector);

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
          (byte) 0x0C, (byte) 0x90, (byte) 0xA9, (byte) 0xD4, (byte) 0x0A, (byte) 0x3A, (byte) 0x96, (byte) 0x31,
          (byte) 0x18, (byte) 0x2E, (byte) 0x9C, (byte) 0xFE, (byte) 0x5E, (byte) 0x42, (byte) 0x0A, (byte) 0x6A,
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
          (byte) 0xAF, (byte) 0x86, (byte) 0xB1, (byte) 0x8F, (byte) 0x3B, (byte) 0x02, (byte) 0xED, (byte) 0x5B,
          (byte) 0xDF, (byte) 0xAF, (byte) 0xCF, (byte) 0x3B, (byte) 0x59, (byte) 0x7A, (byte) 0x18, (byte) 0x1D,
          (byte) 0x30, (byte) 0xA9, (byte) 0xC7, (byte) 0xFF, (byte) 0xDF, (byte) 0x06, (byte) 0xE4, (byte) 0xA2,
          (byte) 0x72, (byte) 0x6C, (byte) 0x68, (byte) 0x26, (byte) 0x56, (byte) 0xBF, (byte) 0x6E, (byte) 0x29,
          (byte) 0x66, (byte) 0xCC, (byte) 0x79, (byte) 0xAF
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
        { (byte) 0x4B, (byte) 0x05, (byte) 0xCA, (byte) 0xFD, (byte) 0x93, (byte) 0xE3, (byte) 0x15 },
        { (byte) 0x91, (byte) 0xFF, (byte) 0xFD, (byte) 0x04, (byte) 0xDA, (byte) 0xC5, (byte) 0xE4 },
        { (byte) 0x6E, (byte) 0x27, (byte) 0xF7, (byte) 0x83, (byte) 0x4E, (byte) 0xEB, (byte) 0xDB },
        { (byte) 0x3B, (byte) 0x70, (byte) 0xA0, (byte) 0xE8, (byte) 0x7E, (byte) 0x9A, (byte) 0x8E },
        { (byte) 0x59, (byte) 0x06, (byte) 0x80, (byte) 0xC0, (byte) 0x2E, (byte) 0xC2, (byte) 0x92 },
        { (byte) 0x6F, (byte) 0xA1, (byte) 0x99, (byte) 0x53, (byte) 0xFA, (byte) 0x82, (byte) 0x58 },
        { (byte) 0x87, (byte) 0xAC, (byte) 0xE7, (byte) 0x29, (byte) 0x52, (byte) 0x72, (byte) 0xBF },
        { (byte) 0x52, (byte) 0xB6, (byte) 0x43, (byte) 0xC9, (byte) 0xB3, (byte) 0x29, (byte) 0xDD },
        { (byte) 0x9B, (byte) 0x10, (byte) 0x66, (byte) 0xA5, (byte) 0xDB, (byte) 0x1D, (byte) 0xC0 },
        { (byte) 0x97, (byte) 0x01, (byte) 0x20, (byte) 0xF7, (byte) 0x7D, (byte) 0x04, (byte) 0x24 },
        { (byte) 0xB0, (byte) 0x2B, (byte) 0xCD, (byte) 0xE3, (byte) 0x06, (byte) 0x8E, (byte) 0x11 },
    };
  }

  BigInteger[] deltasProvider() {
    return new BigInteger[] {
        new BigInteger("0CB803D5AB15D23E", 16),
        new BigInteger("D4B7538FA95D9E8F", 16),
        new BigInteger("8837E85E8F4DD256", 16),
        new BigInteger("89067935879CF65C", 16),
        new BigInteger("2E72E9AC777404B0", 16),
        new BigInteger("C9CAF4253EEFA1B7", 16),
        new BigInteger("00B3DAA3C7110D2D", 16),
        new BigInteger("ECA2FA29F1AB2AF0", 16),
        new BigInteger("FFFFFFFFFFFFFFFF", 16),
        new BigInteger("F000000000000000", 16),
    };
  }

  byte[][] initVectorsProvider() {
    return new byte[][] {
        {
          (byte) 0xE6, (byte) 0x7F, (byte) 0xF9, (byte) 0xAF,
          (byte) 0x07, (byte) 0xCE, (byte) 0x24, (byte) 0xB9,
        },
        {
          (byte) 0x25, (byte) 0x15, (byte) 0x98, (byte) 0xCF,
          (byte) 0x95, (byte) 0x6F, (byte) 0xFD, (byte) 0x0F,
        },
        {
          (byte) 0x43, (byte) 0xA9, (byte) 0x84, (byte) 0xBD,
          (byte) 0xA7, (byte) 0xF9, (byte) 0x7E, (byte) 0x98,
        },
        {
          (byte) 0x40, (byte) 0xC2, (byte) 0xE8, (byte) 0xBB,
          (byte) 0x0A, (byte) 0x16, (byte) 0x87, (byte) 0x4D,
        },
        {
          (byte) 0x91, (byte) 0xA2, (byte) 0x5A, (byte) 0x83,
          (byte) 0xE7, (byte) 0x28, (byte) 0xA9, (byte) 0x3F,
        },
        {
          (byte) 0x13, (byte) 0x3D, (byte) 0xB0, (byte) 0x79,
          (byte) 0x69, (byte) 0xB3, (byte) 0xC8, (byte) 0x45,
        },
        {
          (byte) 0xCD, (byte) 0x57, (byte) 0x96, (byte) 0x95,
          (byte) 0xB3, (byte) 0xC5, (byte) 0xCA, (byte) 0x13,
        },
        {
          (byte) 0x9A, (byte) 0x63, (byte) 0x79, (byte) 0x09,
          (byte) 0xD5, (byte) 0xAF, (byte) 0x57, (byte) 0xED,
        },
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

  void writeToFile(String path, byte[] arr) throws IOException {
    try (FileChannel fileChannel = FileChannel.open(Path.of(path),
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
      @SuppressWarnings("unused")
      int written = fileChannel.write(ByteBuffer.wrap(arr));
    }
  }

  byte[] readFromFile(String path) throws IOException {
    try (FileChannel fileChannel = FileChannel.open(Path.of(path), StandardOpenOption.READ)) {
      byte[] arr = new byte[(int) fileChannel.size()];
      fileChannel.read(ByteBuffer.wrap(arr));
      return arr;
    }
  }

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