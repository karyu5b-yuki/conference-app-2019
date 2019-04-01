package io.github.droidkaigi.confsched2019.settings.ui

import androidx.lifecycle.LiveData
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.toLiveData
import io.github.droidkaigi.confsched2019.model.SettingContents
import io.github.droidkaigi.confsched2019.store.Store
import kotlinx.coroutines.channels.map
import javax.inject.Inject
import javax.inject.Singleton


class SettingsStore @Inject constructor(
    dispatcher: Dispatcher
) : Store() {

    val settingsResult: LiveData<SettingContents> = dispatcher

        .subscribe<Action.SettingContentsChanged>()
        .map { SettingContents(it.contents) }
        .toLiveData(this, null)
}

