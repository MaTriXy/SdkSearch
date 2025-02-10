package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.byteunits.BinaryByteUnit.MEBIBYTES
import com.jakewharton.sdksearch.api.dac.DacComponent
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.store.item.ItemStore
import com.jakewharton.sdksearch.sync.ItemSynchronizer
import dagger.Module
import dagger.Provides
import java.io.File
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import okhttp3.logging.HttpLoggingInterceptor.Logger
import timber.log.Timber
import timber.log.debug

@Module
object SearchPresenterModule {
  @JvmStatic
  @Provides
  fun provideSearchPresenter(
    application: Application,
    store: ItemStore
  ): SearchPresenter {
    val cacheDir = application.cacheDir / "http"

    val logger = Timber.tagged("HTTP")
    val client = OkHttpClient.Builder()
        .cache(Cache(cacheDir, MEBIBYTES.toBytes(10)))
        .addNetworkInterceptor(
            HttpLoggingInterceptor(object : Logger {
              override fun log(message: String) = logger.debug { message }
            }).apply { level = BASIC })
        .build()

    val service = DacComponent.create(client)
        .documentationService()

    val synchronizer = ItemSynchronizer(store, service)

    return SearchPresenter(store, synchronizer)
  }

  private operator fun File.div(pathSegment: String) = File(this, pathSegment)
}
