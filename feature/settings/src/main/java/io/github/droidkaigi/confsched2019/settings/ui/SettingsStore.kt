package io.github.droidkaigi.confsched2019.settings.ui

import androidx.lifecycle.ViewModel
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.android.toLiveData
import kotlinx.coroutines.channels.map
import javax.inject.Inject

class SettingsStore @Inject constructor(
    dispatcher: Dispatcher
) : ViewModel() {

    val settingsResult = dispatcher
        // subscribeのあとに帰ってくるdataの形は、
        // data class SettingContentsLoaded(
        // val contents: List<SettingContent>
        // ) : Action()
        .subscribe<Action.SettingContentsChanged>()
        // mapは変換するという意味。Action..SettingContentsLoadedから利用したいcontentsを取得して、伝搬したい。
        .map { it.contents }
        .toLiveData(Action.SettingContent.EMPTY)
}
