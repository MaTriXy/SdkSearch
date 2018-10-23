package com.jakewharton.sdksearch.api.dac

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.stringBased
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.JSON
import okhttp3.HttpUrl
import okhttp3.MediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

@Module
internal object DacModule {
  @JvmStatic
  @Provides
  fun documentationService(baseUrl: HttpUrl, client: OkHttpClient): DocumentationService {
    val contentType = MediaType.get("application/json; charset=utf-8")
    val json = JSON.nonstrict

    val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(stringBased(contentType, json::parse, json::stringify))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()

    return retrofit.create(DocumentationService::class.java)
  }
}
