package org.client.crypto.permutations;

import org.client.crypto.enums.IndexingRule;

import java.util.List;

public class Permutations {
  public static byte[] rearrange(final byte[] text, List<Integer> blockP, IndexingRule rule) {
    int textLength = text.length;
    int length = blockP.size() / 8;
    if (blockP.size() % 8 != 0) {
      length++;
    }

    byte[] res = new byte[length];
    int indexRes = 0;

    int startIndex = 0;
    boolean lowest = false;
    if (rule == IndexingRule.LOWEST1 || rule == IndexingRule.HIGHEST1) {
      startIndex = 1;
    }
    if (rule == IndexingRule.LOWEST1 || rule == IndexingRule.LOWEST0) {
      lowest = true;
      indexRes = length * 8 - 1;
    }

    for (Integer index : blockP) {
      index -= startIndex;

      if (index >= textLength * 8 || index < 0) {
        throw new IndexOutOfBoundsException("Index of the permutation block P out of bounds. Index: " + index);
      }

      if (lowest) {
        res[indexRes / 8] |= (byte) getBit(index, text[textLength - 1 - index / 8], lowest, indexRes);
        indexRes--;
      } else {
        res[indexRes / 8] |= (byte) getBit(index, text[index / 8], lowest, indexRes);
        indexRes++;
      }

//        System.out.println(index + "   " + getBit(index, text[textLength - 1 - index / 8], lowest, indexRes));
    }

    return res;
  }

  private static byte getBit(int bitIndex, byte value, boolean lowest, int resIndex) {
    int bit;

    if (lowest) {
      bit = (Byte.toUnsignedInt(value) >>> (bitIndex % 8)) & 1;
    } else {
      bit = (Byte.toUnsignedInt(value) >>> (7 - bitIndex % 8)) & 1;
    }

    return (byte) (bit << (7 - resIndex % 8));
  }
}
