package com.jakewharton.sdksearch

import android.app.Application
import com.jakewharton.sdksearch.debug.updater.UpdateActivityModule
import com.jakewharton.sdksearch.ui.MainActivityModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [
  UpdateActivityModule::class,
  UpdateConfigModule::class,
  MainActivityModule::class,
  ItemStoreModule::class,
  SearchPresenterModule::class,
  AndroidInjectionModule::class
])
interface DebugAppComponent : AppComponent {
  @Component.Factory
  interface Factory {
    fun create(@BindsInstance application: Application): DebugAppComponent
  }
}

fun Application.createAppComponent() = DaggerDebugAppComponent.factory().create(this)
