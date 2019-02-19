package io.github.droidkaigi.confsched2019.settings.ui

import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.action.SettingContent
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.toLiveData
import io.github.droidkaigi.confsched2019.store.Store
import kotlinx.coroutines.channels.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsStore @Inject constructor(
    dispatcher: Dispatcher
) : Store() {

    val settingsResult = dispatcher
        // subscribeのあとに帰ってくるdataの形は、
        // data class SettingContentsLoaded(
        // val contents: List<SettingContent>
        // ) : Action()
        .subscribe<Action.SettingContentsChanged>()
        // mapは変換するという意味。Action..SettingContentsLoadedから利用したいcontentsを取得して、伝搬したい。
        .map { it.contents }
        .toLiveData(this, SettingContent.EMPTY)
}
