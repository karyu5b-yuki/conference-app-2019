package io.github.droidkaigi.confsched2019.settings.ui.ui.actioncreator

import androidx.lifecycle.Lifecycle
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.android.coroutineScope
import io.github.droidkaigi.confsched2019.util.SessionAlarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@PageScope
class SettingsActionCreator @Inject constructor(
    override val dispatcher: Dispatcher,
    private val settingsRepository: SettingsRepository,
    @PageScope private val lifecycle: Lifecycle,
    private val settingsAlarm: SettingsAlarm
) : CoroutineScope by lifecycle.coroutineScope,
    ErrorHandler {
    fun refresh() = launch {
        try {
            Timber.debug { "SettingsContentsActionCreator: refresh start" }
            dispatcher.dispatchLoadingState(LoadingState.LOADING)
            Timber.debug { "SettingsContentsActionCreator: At first, load db data" }
            // At first, load db data
            val settingsContents = settingsRepository.settingsContents()
            dispatcher.dispatch(Action.SettingsContentsLoaded(settingsContents))

            // fetch api data
            Timber.debug { "SettingsContentsActionCreator: fetch api data" }
            settingsRepository.refresh()

            // reload db data
            Timber.debug { "SettingsContentsActionCreator: reload db data" }
            val refreshedSettingsContents = settingsRepository.settingsContents()
            dispatcher.dispatch(Action.SettingsContentsLoaded(refreshedSettingsContents))
            Timber.debug { "SettingsContentsActionCreator: refresh end" }
            dispatcher.dispatchLoadingState(LoadingState.LOADED)
        } catch (e: Exception) {
            onError(e)
            dispatcher.dispatchLoadingState(LoadingState.INITIALIZED)
        }
    }

    fun load() {
        launch {
            try {
                dispatcher.dispatchLoadingState(LoadingState.LOADING)
                val settingsContents = settingsRepository.settingsContents()
                dispatcher.dispatch(Action.SettingsContentsLoaded(settingsContents))
                dispatcher.dispatchLoadingState(LoadingState.LOADED)
            } catch (e: Exception) {
                onError(e)
                dispatcher.dispatchLoadingState(LoadingState.INITIALIZED)
            }
        }
    }

    fun toggleFavorite(session: Session) {
        launch {
            try {
                settingsRepository.toggleFavorite(session)
                settingsAlarm.toggleRegister(session)
                val settingsContents = settingsRepository.settingsContents()
                dispatcher.dispatch(Action.SettingsContentsLoaded(settingsContents))
            } catch (e: Exception) {
                dispatcher.dispatch(
                    Action.Error(ErrorMessage.of(
                        R.string.settings_favorite_connection_error, e)
                    )
                )
            }
        }
    }

    private suspend fun Dispatcher.dispatchLoadingState(loadingState: LoadingState) {
        dispatch(Action.SettingsLoadingStateChanged(loadingState))
    }
}
