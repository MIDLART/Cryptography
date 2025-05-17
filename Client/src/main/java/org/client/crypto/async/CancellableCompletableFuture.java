package org.client.crypto.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Log4j2
@RequiredArgsConstructor
public class CancellableCompletableFuture<T> extends CompletableFuture<T> {
  private final AtomicBoolean cancelled;
  private final EncryptionProgress progress;

  public static <U> CancellableCompletableFuture<U> runAsync(Runnable runnable, AtomicBoolean cancelled, EncryptionProgress progress) {
    CancellableCompletableFuture<U> future = new CancellableCompletableFuture<>(cancelled, progress);
    CompletableFuture.runAsync(runnable).whenComplete((result, ex) -> {
      if (ex != null) {
        future.completeExceptionally(ex);
      } else {
        future.complete(null);
      }
    });
    return future;
  }

  public static <U> CancellableCompletableFuture<U> supplyAsync(Supplier<U> supplier, AtomicBoolean cancelled, EncryptionProgress progress) {
    CancellableCompletableFuture<U> future = new CancellableCompletableFuture<>(cancelled, progress);
    CompletableFuture.supplyAsync(supplier).whenComplete((result, ex) -> {
      if (ex != null) {
        future.completeExceptionally(ex);
      } else {
        future.complete(result);
      }
    });
    return future;
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    cancelled.set(true);
    return super.cancel(mayInterruptIfRunning);
  }

  public double getProgress() {
    if (progress.getTotal() == 0) {
      return 0;
    }

    if (progress.getProcessed() > progress.getTotal()) {
      log.error("More than possible processed: {} / {}", progress.getProcessed(), progress.getTotal());
    }

    return (double) progress.getProcessed() / progress.getTotal() * 100;
  }
}
