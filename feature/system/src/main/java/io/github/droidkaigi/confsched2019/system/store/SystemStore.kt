package io.github.droidkaigi.confsched2019.system.store

import androidx.lifecycle.LiveData
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.toLiveDataEndless
import io.github.droidkaigi.confsched2019.ext.toSingleLiveDataEndless
import io.github.droidkaigi.confsched2019.model.CopyText
import io.github.droidkaigi.confsched2019.model.Lang
import io.github.droidkaigi.confsched2019.model.Message
import io.github.droidkaigi.confsched2019.model.SystemProperty
import io.github.droidkaigi.confsched2019.model.WifiConfiguration
import kotlinx.coroutines.channels.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemStore @Inject constructor(
    dispatcher: Dispatcher
) {
    val message: LiveData<Message?> = dispatcher
        .subscribe<Action.ShowProcessingMessage>()
        .map { it.message }
        .toSingleLiveDataEndless(null)

    val systemProperty: LiveData<SystemProperty> = dispatcher
        .subscribe<Action.SystemPropertyLoaded>()
        .map { it.system }
        .toLiveDataEndless(SystemProperty(Lang.EN))
    val lang
        get() = systemProperty.value!!.lang

    val wifiConfiguration: LiveData<WifiConfiguration?> = dispatcher
        .subscribe<Action.WifiConfigurationChange>()
        .map { it.wifiConfiguration }
        .toSingleLiveDataEndless(null)

    val copyText: LiveData<CopyText?> = dispatcher
        .subscribe<Action.ClipboardChange>()
        .map { it.copyText }
        .toSingleLiveDataEndless(null)
}
