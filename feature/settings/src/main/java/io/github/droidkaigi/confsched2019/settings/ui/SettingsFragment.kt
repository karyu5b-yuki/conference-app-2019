package io.github.droidkaigi.confsched2019.settings.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Lifecycle
//import androidx.preference.PreferenceActionCreator
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.di.PageScope
import javax.inject.Inject

class SettingsFragment : DaggerFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var preferenceActionCreator: PreferenceActionCreator
    @Inject lateinit var settingsStore: SettingsStore
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(io.github.droidkaigi.confsched2019.settings.R.xml.preferences)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        settingsStore.settingsResult.changed(viewLifecycleOwner) {settingsContents ->
            for (content in settingContents.preferences) {
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putBoolean(content.key, content.value ?: false)
                editor.apply()
            }

            Log.d("settingContents", settingContents.toString())
                 }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }
    override fun onDestroy() {
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val contents = mutableMapOf<String, Boolean?>()
        val changed_title = sharedPreferences?. getBoolean(
            "session_title", false
        )
        val changed_url = sharedPreferences?. getBoolean(
            "session_url", false
        )
        val changed_event = sharedPreferences?.getBoolean(
            "event_hashtag", false
        )
        val changed_room = sharedPreferences?.getBoolean(
            "room_hashtag", false
        )

        contents["session_title"] = changed_title
        contents["session_url"] = changed_url
        contents["event_hashtag"] = changed_event
        contents["room_hashtag"] = changed_room

        preferenceActionCreator.submit(Action.SettingContentsChanged(contents))
    }
    @Module
    abstract class SettingsFragmentModule{
        @Module
        companion object {
            @PageScope
            @JvmStatic
            @Provides
            fun providesLifecycle(
                settingsFragment: SettingsFragment
            ): Lifecycle{
                return settingsFragment.viewLifecycleOwner.lifecycle
            }
        }
}
