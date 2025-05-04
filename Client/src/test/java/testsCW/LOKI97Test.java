package testsCW;

import org.client.crypto.SymmetricEncryption;
import org.client.crypto.loki97.LOKI97;
import org.client.crypto.loki97.impl.LOKI97FeistelFunction;
import org.client.crypto.loki97.impl.LOKI97GenKey;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class LOKI97Test {

  @Test
  void testLOKI97GenKey() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13,
            (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17,
            (byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B,
            (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F
    };
    List<Long> expectedOutput = List.of(
            0xECB82110452BF90AL, 0x592CD965E4168E33L, 0x1A8E5818A655138CL, 0x86E3263EBBBF339CL,
            0xAD6DFD04A887509BL, 0xB3A56496E96B4F0DL, 0x52A0073EA724A264L, 0x2FF1BF749D54136BL,
            0xBD0BC040D3A54F28L, 0x929220E7076443A8L, 0x7FB7D2666220233AL, 0x56FE853A068B8D2EL,
            0xAC1C43D92F7490B6L, 0xFBE3491706663C44L, 0xE4AC5DE7F5220AB5L, 0xF10D78B02017847CL,
            0xF4FFDEBD5E175312L, 0x4396B72AE367FF41L, 0x26ED088C13C3993FL, 0xC3468074E387B643L,
            0x2752EDD11A129E73L, 0xA46AACF6DD57D61FL, 0x5F06EB99CFD8084FL, 0x8252416503BE6B13L,
            0xEF7D17F4791630C3L, 0xB5536290E13AAD94L, 0xD65073A787DCAF9AL, 0x3DB329BD5B9BF213L,
            0x804E42039A6496DAL, 0xDB97B9E35223D540L, 0xB152C3DD7A6EE03FL, 0x176EECE0F5AA3E62L,
            0xF0B4C6DA31B841FCL, 0x3BDDEA965A9F612DL, 0xE03718A6FDC7901AL, 0x710587AB3E6A614FL,
            0xB0C6F115D3ECE6C2L, 0xAF82DA2EF75F6924L, 0xAA5DE8BDB42A8BDBL, 0x50BB552A21F75E7DL,
            0xB8EB467438FF42E4L, 0x936362030FA48C95L, 0xE55434C694CE74CEL, 0xBDA3575166DF26BCL,
            0xB779C086BDB9551EL, 0x1322E154E6746255L, 0x3441894738B21D3DL, 0xF9539B20F3944405L
    );

    // EXECUTION
    LOKI97FeistelFunction f = new LOKI97FeistelFunction(initS1(), initS2());
    LOKI97GenKey genKey = new LOKI97GenKey(key.length, f);
    List<Long> actualOutput = genKey.genKey(key);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testLOKI97SimpleCycle() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00
    };
    byte[] input = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };
    byte[] expectedOutput = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
    };

    // EXECUTION
    SymmetricEncryption loki97 = new LOKI97(key, initS1(), initS2());
    byte[] actualOutput = loki97.decryption(loki97.encryption(input));

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testLOKI97Encryption() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13,
            (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17,
            (byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B,
            (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F
    };

    byte[] input = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
    };

    byte[] expectedOutput = {
            (byte) 0x75, (byte) 0x08, (byte) 0x0E, (byte) 0x35,
            (byte) 0x9F, (byte) 0x10, (byte) 0xFE, (byte) 0x64,
            (byte) 0x01, (byte) 0x44, (byte) 0xB3, (byte) 0x5C,
            (byte) 0x57, (byte) 0x12, (byte) 0x8D, (byte) 0xAD
    };

    // EXECUTION
    SymmetricEncryption loki97 = new LOKI97(key, initS1(), initS2());
    byte[] actualOutput = loki97.encryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testLOKI97Decryption() {
    // SETUP
    byte[] key = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13,
            (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x17,
            (byte) 0x18, (byte) 0x19, (byte) 0x1A, (byte) 0x1B,
            (byte) 0x1C, (byte) 0x1D, (byte) 0x1E, (byte) 0x1F
    };

    byte[] input = {
            (byte) 0x75, (byte) 0x08, (byte) 0x0E, (byte) 0x35,
            (byte) 0x9F, (byte) 0x10, (byte) 0xFE, (byte) 0x64,
            (byte) 0x01, (byte) 0x44, (byte) 0xB3, (byte) 0x5C,
            (byte) 0x57, (byte) 0x12, (byte) 0x8D, (byte) 0xAD
    };

    byte[] expectedOutput = {
            (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x07,
            (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B,
            (byte) 0x0C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F
    };

    // EXECUTION
    SymmetricEncryption loki97 = new LOKI97(key, initS1(), initS2());
    byte[] actualOutput = loki97.decryption(input);

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }

  @Test
  void testLOKI97Cycle() {
    // SETUP
    byte[] key = {
            (byte) 0xFF, (byte) 0x11, (byte) 0x02, (byte) 0x03,
            (byte) 0x04, (byte) 0x05, (byte) 0x06, (byte) 0x0B,
            (byte) 0x08, (byte) 0x19, (byte) 0x01, (byte) 0x0B,
            (byte) 0x7C, (byte) 0x0D, (byte) 0x0E, (byte) 0x0F,
            (byte) 0x10, (byte) 0x11, (byte) 0x12, (byte) 0x13,
            (byte) 0x14, (byte) 0x15, (byte) 0x16, (byte) 0x1F
    };

    byte[] input = {
            (byte) 0x76, (byte) 0xBB, (byte) 0x0E, (byte) 0x37,
            (byte) 0x9F, (byte) 0xAA, (byte) 0xFE, (byte) 0x64,
            (byte) 0xCB, (byte) 0x6E, (byte) 0xB7, (byte) 0x5C,
            (byte) 0xEF, (byte) 0x17, (byte) 0xDD, (byte) 0xDD
    };

    byte[] expectedOutput = {
            (byte) 0x76, (byte) 0xBB, (byte) 0x0E, (byte) 0x37,
            (byte) 0x9F, (byte) 0xAA, (byte) 0xFE, (byte) 0x64,
            (byte) 0xCB, (byte) 0x6E, (byte) 0xB7, (byte) 0x5C,
            (byte) 0xEF, (byte) 0x17, (byte) 0xDD, (byte) 0xDD
    };

    // EXECUTION
    SymmetricEncryption loki97 = new LOKI97(key, initS1(), initS2());
    byte[] actualOutput = loki97.decryption(loki97.encryption(input));

    // ASSERTION
    Assert.assertEquals(actualOutput, expectedOutput);
  }
}
