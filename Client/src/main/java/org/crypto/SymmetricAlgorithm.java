package org.crypto;

import org.crypto.block.*;
import org.crypto.enums.EncryptOrDecrypt;
import org.crypto.enums.EncryptionMode;
import org.crypto.enums.PackingMode;
import org.crypto.modes.Packing;

import java.io.File;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.crypto.operations.ArrayOperations.listToArray;
import static org.crypto.operations.BitOperations.xor;
import static org.crypto.enums.EncryptOrDecrypt.*;

public class SymmetricAlgorithm {
  private final SymmetricEncryption symmetricEncryption;
  private final EncryptionMode encryptionMode;

  private final Packing packing;

  private final int blockSize;
  private final byte[] IV;

  private BigInteger RD = BigInteger.valueOf(1);
  private final BigInteger rdMax;

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption, EncryptionMode encryptionMode, PackingMode packingMode) {
    this.symmetricEncryption = symmetricEncryption;
    this.encryptionMode = encryptionMode;
    blockSize = symmetricEncryption.getBlockSize();

    this.IV = new byte[blockSize];
    new SecureRandom().nextBytes(IV);

    rdMax = BigInteger.valueOf(2).pow(8 * blockSize);

    packing = new Packing(blockSize, packingMode);
  }

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption, EncryptionMode encryptionMode, PackingMode packingMode, byte[] IV) {
    this.symmetricEncryption = symmetricEncryption;
    this.encryptionMode = encryptionMode;
    blockSize = symmetricEncryption.getBlockSize();

    this.IV = new byte[blockSize];
    System.arraycopy(IV, 0, this.IV, 0, blockSize);

    rdMax = BigInteger.valueOf(2).pow(8 * blockSize);

    packing = new Packing(blockSize, packingMode);
  }

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption, EncryptionMode encryptionMode, PackingMode packingMode,
                            byte[] IV, BigInteger delta) {
    this.symmetricEncryption = symmetricEncryption;
    this.encryptionMode = encryptionMode;
    blockSize = symmetricEncryption.getBlockSize();

    this.IV = new byte[blockSize];
    System.arraycopy(IV, 0, this.IV, 0, blockSize);

    rdMax = BigInteger.valueOf(2).pow(8 * blockSize);

    this.RD = delta;

    packing = new Packing(blockSize, packingMode);
  }

  public CompletableFuture<Void> encryptAsync(byte[] input, byte[] output) {
    return CompletableFuture.runAsync(() -> encrypt(input, output));
  }

  public CompletableFuture<Void> encryptAsync(String inputFile, String outputFile) {
    return CompletableFuture.runAsync(() -> encrypt(inputFile, outputFile));
  }

  public CompletableFuture<byte[]> encryptAsync(byte[] input) {
    return CompletableFuture.supplyAsync(() -> encrypt(input));
  }

  public CompletableFuture<Void> decryptAsync(byte[] input, byte[] output) {
    return CompletableFuture.runAsync(() -> decrypt(input, output));
  }

  public CompletableFuture<Void> decryptAsync(String inputFile, String outputFile) {
    return CompletableFuture.runAsync(() -> decrypt(inputFile, outputFile));
  }

  public CompletableFuture<byte[]> decryptAsync(byte[] input) {
    return CompletableFuture.supplyAsync(() -> decrypt(input));
  }

  public byte[] encrypt(byte[] input) {
    int inputLength = input.length;

    byte[] output = new byte[getBlockCount(inputLength, ENCRYPT) * blockSize];

    encrypt(input, output);

    return output;
  }

  public void encrypt(byte[] input, byte[] output) {
    int inputLength = input.length;

    if (inputLength == 0) {
      throw new IllegalArgumentException("Input is empty");
    }

    if (output.length < getBlockCount(inputLength, ENCRYPT) * blockSize) {
      throw new IllegalArgumentException("Output is too small");
    }

    ReadBlock readBlock = new ArrayRead(input, blockSize, packing::fill);
    WriteBlock writeBlock = new ArrayWrite(output, blockSize);

    encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, ENCRYPT), ENCRYPT);
  }

  public void encrypt(String inputFile, String outputFile) {
    fileErrorCheck(inputFile, outputFile);
    long inputLength = new File(inputFile).length();

    ReadBlock readBlock = new FileRead(inputFile, blockSize, packing::fill);
    WriteBlock writeBlock = new FileWrite(outputFile, blockSize);

    encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, ENCRYPT), ENCRYPT);
  }

  public void decrypt(byte[] input, byte[] output) {
    int inputLength = input.length;

    if (inputLength == 0) {
      throw new IllegalArgumentException("Input is empty");
    }

    if (inputLength != output.length) {
      throw new IllegalArgumentException("Invalid output length");
    }

    ReadBlock readBlock = new ArrayRead(input, blockSize);
    WriteBlock writeBlock = new ArrayWrite(output, blockSize, packing::unpack);

    encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);
  }

  public byte[] decrypt(byte[] input) {
    int inputLength = input.length;

    if (inputLength == 0) {
      throw new IllegalArgumentException("Input is empty");
    }

    List<byte[]> outputList =
            Collections.synchronizedList(
            new ArrayList<>(
                    Collections.nCopies(getBlockCount(inputLength, DECRYPT), new byte[0])));

    ReadBlock readBlock = new ArrayRead(input, blockSize);
    WriteBlock writeBlock = new ListWrite(outputList, blockSize, inputLength, packing::unpack);

    encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);

    return listToArray(outputList);
  }

  public void decrypt(String inputFile, String outputFile) {
    fileErrorCheck(inputFile, outputFile);
    long inputLength = new File(inputFile).length();

    ReadBlock readBlock = new FileRead(inputFile, blockSize);
    WriteBlock writeBlock = new FileWrite(outputFile, blockSize, inputLength, packing::unpack);

    encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);
  }

  private void encryptionMode(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    switch (encryptionMode) {
      case EncryptionMode.ECB -> ECB(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.CBC -> CBC(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.CFB -> CFB(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.CTR -> CTR(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.OFB -> OFB(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.PCBC -> PCBC(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case EncryptionMode.RandomDelta -> RandomDelta(readBlock, writeBlock, blockCount, encryptOrDecrypt);
    }
  }

  private void fileErrorCheck(String inputFile, String outputFile) {
    if (inputFile == null || inputFile.isEmpty() ||
            !(new File(inputFile).exists())) {
      throw new RuntimeException("Input file not found");
    }

    if (outputFile == null || outputFile.isEmpty()) {
      throw new RuntimeException("Output file name is empty");
    }

    if (inputFile.equals(outputFile)) {
      throw new RuntimeException("Input and output files cannot be the same");
    }
  }

  private int getBlockCount(long length, EncryptOrDecrypt encryptOrDecrypt) {
    int blockCount = (int) (length / blockSize);
    if (encryptOrDecrypt == ENCRYPT && packing.getMode() != PackingMode.NO) {
      blockCount++;
    }

    return blockCount;
  }

  /// Encryption Modes

  private void ECB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function = encryptOrDecrypt == ENCRYPT ?
            symmetricEncryption::encryption : symmetricEncryption::decryption;

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, function.apply(buffer));
    });
  }

  private void CBC(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      function = symmetricEncryption::encryption;

      byte[] c = function.apply(xor(first, IV));
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {
        byte[] m = readBlock.get(i);
        c = function.apply(xor(m, c));

        writeBlock.put(i, c);
      }
    } else {
      function = symmetricEncryption::decryption;

      writeBlock.put(0, xor(function.apply(first), IV));

      IntStream.range(1, blockCount).parallel().forEach(i -> {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);

        writeBlock.put(i, xor(function.apply(c), prevC));
      });
    }
  }

  private void OFB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function = symmetricEncryption::encryption;

    byte[] E = IV;

    for (int i = 0; i < blockCount; i++) {
      byte[] buffer = readBlock.get(i);
      E = function.apply(E);

      writeBlock.put(i, xor(E, buffer));
    }
  }

  private void CFB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function = symmetricEncryption::encryption;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      byte[] c = xor(function.apply(IV), first);
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {
        byte[] m = readBlock.get(i);
        c = xor(function.apply(c), m);

        writeBlock.put(i, c);
      }
    } else {
      writeBlock.put(0, xor(function.apply(IV), first));

      IntStream.range(1, blockCount).parallel().forEach(i -> {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);

        writeBlock.put(i, xor(function.apply(prevC), c));
      });
    }
  }

  private void PCBC(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      function = symmetricEncryption::encryption;

      byte[] c = function.apply(xor(first, IV));
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {
        byte[] m = readBlock.get(i);
        byte[] prevM = readBlock.get(i - 1);
        c = function.apply(xor(xor(m, c), prevM));

        writeBlock.put(i, c);
      }
    } else {
      function = symmetricEncryption::decryption;

      byte[] m = xor(function.apply(first), IV);
      writeBlock.put(0, m);

      for (int i = 1; i < blockCount; i++) {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);
        m = xor(xor(function.apply(c), prevC), m);

        writeBlock.put(i, m);
      }
    }
  }

  private void CTR(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function = symmetricEncryption::encryption;
    BigInteger count = new BigInteger(IV);

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, xor(buffer, function.apply(counter(count, BigInteger.valueOf(i)))));
    });
  }

  private void RandomDelta(ReadBlock readBlock, WriteBlock writeBlock, int blockCount, EncryptOrDecrypt encryptOrDecrypt) {
    Function<byte[], byte[]> function = symmetricEncryption::encryption;
    BigInteger count = new BigInteger(IV);

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, xor(buffer, function.apply(counter(count, modularMultiply(RD, i)))));
    });
  }

  private byte[] counter(BigInteger num, BigInteger n) {
    byte[] res = new byte[blockSize];

    byte[] sum = num.add(n).toByteArray();

    int index = blockSize - 1;
    int sumIndex = sum.length - 1;
    for (; sumIndex >= 0 && index >= 0; sumIndex--, index--) {
      res[index] = sum[sumIndex];
    }

    return res;
  }

  private BigInteger modularMultiply(BigInteger rd, int i) {
    BigInteger b = BigInteger.valueOf(i);

    return rd.multiply(b).mod(rdMax);
  }
}
