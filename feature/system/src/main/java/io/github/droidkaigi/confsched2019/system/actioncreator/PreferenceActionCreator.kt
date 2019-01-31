package io.github.droidkaigi.confsched2019.system.actioncreator

import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.model.SystemProperty
import io.github.droidkaigi.confsched2019.model.defaultLang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus
import javax.inject.Inject

class PreferenceActionCreator @Inject constructor(val dispatcher: Dispatcher) {
    // TOOD コンパイルエラーが出るようであれば、他のActionCreatorのようにScopeを追加する。

    fun submit(action: Action) {
        dispatcher.dispatch(action)
    }

}
