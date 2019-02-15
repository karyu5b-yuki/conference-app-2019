package io.github.droidkaigi.confsched2019.user.store

import androidx.lifecycle.LiveData
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.dispatcher.Dispatcher
import io.github.droidkaigi.confsched2019.ext.toLiveDataEndless
import kotlinx.coroutines.channels.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserStore @Inject constructor(
    dispatcher: Dispatcher
) {
    val registered: LiveData<Boolean> = dispatcher.subscribe<Action.UserRegistered>()
        .map { true }
        .toLiveDataEndless(false)
}
