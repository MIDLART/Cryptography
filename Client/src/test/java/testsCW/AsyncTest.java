package testsCW;

import org.client.crypto.SymmetricAlgorithm;
import org.client.crypto.async.CancellableCompletableFuture;
import org.client.crypto.des.DES;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static org.client.crypto.enums.EncryptionMode.*;
import static org.client.crypto.enums.PackingMode.ANSIX923;

public class AsyncTest {

  String testDirectory = "src/test/resources/AsyncTests";

  @Test
  void testCancel() throws IOException, ExecutionException, InterruptedException {
    // SETUP
    byte[] key = {
            (byte) 0x55, (byte) 0x46, (byte) 0xEA, (byte) 0xDD, (byte) 0x5C, (byte) 0x59, (byte) 0xE9
    };
    byte[] initVector = {
            (byte) 0x5a, (byte) 0xa8, (byte) 0x08, (byte) 0x43, (byte) 0x19, (byte) 0xe5, (byte) 0xd8, (byte) 0x2d,
    };

    String in = testDirectory + "/picture.jpg";
    String encOut = testDirectory + "/encryptedPictureC";
    String decOut = testDirectory + "/decryptedPicture.jpg";

    // EXECUTION
    var cryptoSystem = new DES(key);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, RandomDelta, ANSIX923, initVector);

    System.out.println(ForkJoinPool.commonPool().getActiveThreadCount());

    CancellableCompletableFuture<Void> encryptFuture = cryptoContext.encryptAsync(in, encOut);
    encryptFuture.get();

    CancellableCompletableFuture<Void> decryptFuture = cryptoContext.decryptAsync(encOut, decOut);

    Thread.sleep(100);

    decryptFuture.cancel(true);

    Thread.sleep(100);
    System.out.println(ForkJoinPool.commonPool().getActiveThreadCount());


    // ASSERTION
    Assert.assertTrue(decryptFuture.isCancelled());
  }

  @Test
  void testPictureFileCycle() throws IOException, ExecutionException, InterruptedException {
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
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ANSIX923, initVector);

    CancellableCompletableFuture<Void> encryptFuture = cryptoContext.encryptAsync(in, encOut);
    encryptFuture.get();

    CancellableCompletableFuture<Void> decryptFuture = cryptoContext.decryptAsync(encOut, decOut);
    decryptFuture.get();

    // ASSERTION
    Assert.assertTrue(areFilesEqual(in, decOut));
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
}
