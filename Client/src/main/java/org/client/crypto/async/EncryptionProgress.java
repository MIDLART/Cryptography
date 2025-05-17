package org.client.crypto.async;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
public class EncryptionProgress {
  private final AtomicLong processed;
  private final AtomicLong total;

  public void increment() {
    processed.incrementAndGet();
  }

  public long getProcessed() {
    return processed.get();
  }

  public long getTotal() {
    return total.get();
  }

  public void setProcessed(long count) {
    processed.set(count);
  }

  public void setTotal(long count) {
    total.set(count);
  }
}
