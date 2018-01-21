package com.jakewharton.sdksearch.store

import android.support.test.InstrumentationRegistry
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ItemStoreTest {
  private val itemStore = DbComponent.builder()
      .context(InstrumentationRegistry.getContext())
      .filename(null)
      .scheduler(Schedulers.trampoline())
      .build()
      .itemStore()

  @Test fun wildcards() {
    runBlocking {
      itemStore.updateItems(listOf(
          Item(1, "com.example", "One%Two", false, "percent.html"),
          Item(2, "com.example", "One_Two", false, "underscore.html"),
          Item(3, "com.example", "One\\Two", false, "escape.html")
      ))
    }

    itemStore.queryItems("%")
        .test()
        .takeValue {
          assertEquals("One%Two", it.single().className)
        }
        .dispose()

    itemStore.queryItems("_")
        .test()
        .takeValue {
          assertEquals("One_Two", it.single().className)
        }
        .dispose()

    itemStore.queryItems("\\")
        .test()
        .takeValue {
          assertEquals("One\\Two", it.single().className)
        }
        .dispose()
  }
}

private fun <I> TestObserver<I>.takeValue(handler: (value: I) -> Unit): TestObserver<I> {
  awaitCount(1)
  handler(values().removeAt(0))
  return this
}
