package com.jakewharton.sdksearch.ui

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
import com.jakewharton.presentation.Presentation
import com.jakewharton.presentation.bindTo
import com.jakewharton.presentation.startPresentation
import com.jakewharton.sdksearch.R
import com.jakewharton.sdksearch.search.presenter.SearchPresenter
import com.jakewharton.sdksearch.search.ui.ClipboardCopyItemHandler
import com.jakewharton.sdksearch.search.ui.OpenDocumentationItemHandler
import com.jakewharton.sdksearch.search.ui.OpenSourceItemHandler
import com.jakewharton.sdksearch.search.ui.SearchUiBinder
import com.jakewharton.sdksearch.search.ui.ShareItemHandler
import dagger.Module
import dagger.android.AndroidInjection
import dagger.android.ContributesAndroidInjector
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import timber.log.error

class MainActivity : Activity() {
  private val scope = MainScope()

  @Inject lateinit var searchPresenterProvider: Provider<SearchPresenter>

  private lateinit var presentation: Presentation

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT == 26 && resources.getBoolean(R.bool.light_navigation)) {
      // This attribute can only be set in code on API 26. It's in the theme in 27+.
      window.decorView.systemUiVisibility = SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
    }

    if ("true" == intent.getStringExtra("crash")) {
      Timber.error { "Synthetic crash signal detected. Throwing in 3.. 2.. 1.." }
      throw RuntimeException("Crash! Bang! Pow! This is only a test...")
    }

    AndroidInjection.inject(this)

    presentation = lastNonConfigurationInstance as Presentation?
        ?: searchPresenterProvider.get().startPresentation(Dispatchers.Main)
    val presenter = presentation.presenter as SearchPresenter

    val onClick = OpenDocumentationItemHandler(this)
    val onCopy = ClipboardCopyItemHandler(this)
    val onShare = ShareItemHandler(this)
    val onSource = OpenSourceItemHandler(this)

    val defaultQuery = if (savedInstanceState == null) {
      val data = intent.data
      if (data != null && data.host == "sdksearch.app" && data.pathSegments.size == 1) {
        SearchUiBinder.Args(data.pathSegments[0])
      } else null
    } else null

    setContentView(R.layout.search)

    scope.launch(Dispatchers.Unconfined) {
      val binder = SearchUiBinder(window.decorView, presenter.events, onClick, onCopy, onShare, onSource)
      defaultQuery?.let { binder.init(it) }

      binder.bindTo(presenter)
    }
  }

  override fun onRetainNonConfigurationInstance() = presentation

  override fun onDestroy() {
    super.onDestroy()
    scope.cancel()

    if (!isChangingConfigurations) {
      presentation.stop()
    }
  }
}

@Module
abstract class MainActivityModule {
  @ContributesAndroidInjector
  abstract fun contributeMainActivity(): MainActivity
}
