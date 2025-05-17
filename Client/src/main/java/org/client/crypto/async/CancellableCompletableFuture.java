package org.client.crypto.async;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class CancellableCompletableFuture<T> extends CompletableFuture<T> {
  private final AtomicBoolean cancelled;

  private CancellableCompletableFuture(AtomicBoolean cancelled) {
    this.cancelled = cancelled;
  }

  public static <U> CancellableCompletableFuture<U> runAsync(Runnable runnable, AtomicBoolean cancelled) {
    CancellableCompletableFuture<U> future = new CancellableCompletableFuture<>(cancelled);
    CompletableFuture.runAsync(runnable).whenComplete((result, ex) -> {
      if (ex != null) {
        future.completeExceptionally(ex);
      } else {
        future.complete(null);
      }
    });
    return future;
  }

  public static <U> CancellableCompletableFuture<U> supplyAsync(Supplier<U> supplier, AtomicBoolean cancelled) {
    CancellableCompletableFuture<U> future = new CancellableCompletableFuture<>(cancelled);
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
    super.cancel(mayInterruptIfRunning);
    return true;
  }
}
