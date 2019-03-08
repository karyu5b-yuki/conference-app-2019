package io.github.droidkaigi.confsched2019.settings.ui

import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.model.SettingContents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import javax.inject.Inject

class PreferenceActionCreator @Inject constructor(
    val dispatcher: Dispatcher)
    : CoroutineScope by GlobalScope + SupervisorJob() {
    fun submit(action: Action)=launch {
            dispatcher.dispatch(action)

    }
}
