package io.github.droidkaigi.confsched2019.settings.ui

import androidx.lifecycle.ViewModel
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import kotlinx.coroutines.channels.map
import javax.inject.Inject


class SettingsStore @Inject constructor(
    dispatcher: Dispatcher
) : ViewModel() {
    val query get() = settingsResult.value?.query

    val settingsResult = dispatcher
        .subscribe<Action.SettingsResultLoaded>()
        .map { it.settingsResult }
        .toLiveData(SettingsResult.EMPTY)
}
