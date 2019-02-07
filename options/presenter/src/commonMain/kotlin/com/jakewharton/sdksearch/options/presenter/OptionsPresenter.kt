package com.jakewharton.sdksearch.options.presenter

import com.jakewharton.presentation.Presenter
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Event
import com.jakewharton.sdksearch.options.presenter.OptionsPresenter.Model
import com.jakewharton.sdksearch.reference.PRODUCTION_DAC
import com.jakewharton.sdksearch.reference.PRODUCTION_GIT_WEB
import com.jakewharton.sdksearch.store.config.Config
import com.jakewharton.sdksearch.store.config.ConfigStore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class OptionsPresenter(
  private val configStore: ConfigStore
) : Presenter<Model, Event> {
  private val _models = ConflatedBroadcastChannel<Model>()
  override val models: ReceiveChannel<Model> get() = _models.openSubscription()

  private val _events = Channel<Event>(RENDEZVOUS)
  override val events: SendChannel<Event> get() = _events

  override suspend fun start() = coroutineScope<Unit> {
    var model = Model()
    fun sendModel(newModel: Model) {
      model = newModel
      _models.offer(newModel)
    }

    launch {
      // TODO make a full channel for real-time updates?
      val config = configStore.load()
      sendModel(model.copy(disableUpdates = false, config = config))
    }

    launch {
      _events.consumeEach {
        when (it) {
          is Event.Save -> {
            sendModel(model.copy(disableUpdates = true, config = it.config))
            configStore.save(it.config)
            sendModel(model.copy(disableUpdates = false))
          }
          is Event.RestoreDefaults -> {
            val config = Config(PRODUCTION_GIT_WEB, PRODUCTION_DAC)
            sendModel(model.copy(disableUpdates = true, config = config))
            configStore.save(config)
            sendModel(model.copy(disableUpdates = false))
          }
        }
      }
    }
  }

  data class Model(
    val disableUpdates: Boolean = true,
    val config: Config? = null
  )

  sealed class Event {
    data class Save(val config: Config) : Event()
    object RestoreDefaults : Event()
  }
}
