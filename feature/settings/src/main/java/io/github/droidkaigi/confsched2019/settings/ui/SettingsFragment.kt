package io.github.droidkaigi.confsched2019.settings.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.model.SettingContents
import io.github.droidkaigi.confsched2019.settings.R
import java.lang.RuntimeException
import javax.inject.Inject

class SettingsFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionTitleValue = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SESSION_TITLE, false)
        val sessionUrlValue = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SESSION_URL, false)
        val eventHashtagValue = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean(EVENT_HUSHTAG, false)
        val roomHashtagValue = android.preference.PreferenceManager.getDefaultSharedPreferences(context).getBoolean(ROOM_HUSHTAG, false)

        val settingContents = SettingContents()
        settingContents.preferences[SESSION_TITLE] = sessionTitleValue
        settingContents.preferences[SESSION_URL] = sessionUrlValue
        settingContents.preferences[EVENT_HUSHTAG] = eventHashtagValue
        settingContents.preferences[ROOM_HUSHTAG] = roomHashtagValue

        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.session_title_key).isChecked = settingContents.preferences[SESSION_TITLE]!!
        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.session_title_key).setOnCheckedChangeListener { buttonView, isChecked ->
            android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(SESSION_TITLE,isChecked)
                .apply()
        }
        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.session_url_key).isChecked = settingContents.preferences [SESSION_URL]!!
        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.session_url_key).setOnCheckedChangeListener { buttonView, isChecked ->
            android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(SESSION_URL, isChecked)
                .apply()
        }

        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.event_hashtag_key).isChecked = settingContents.preferences [EVENT_HUSHTAG]!!
        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.event_hashtag_key).setOnCheckedChangeListener { buttonView, isChecked ->
            android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(EVENT_HUSHTAG, isChecked)
                .apply()
        }


        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.room_hashtag_key).isChecked = settingContents.preferences [ROOM_HUSHTAG]!!
        view.findViewById<CheckBox>(io.github.droidkaigi.confsched2019.settings.R.id.room_hashtag_key).setOnCheckedChangeListener { buttonView, isChecked ->
            android.preference.PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(ROOM_HUSHTAG, isChecked)
                .apply()
        }

    }

    companion object {
        private const val SESSION_TITLE = "session_title_key"
        private const val SESSION_URL = "session_url_key"
        private const val EVENT_HUSHTAG = "event_hashtag_key"
        private const val ROOM_HUSHTAG = "room_hashtag_key"
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}

@Module
abstract class SettingsFragmentModule {
    @Module
    companion object {
        @PageScope
        @JvmStatic
        @Provides
        fun providesLifecycle(
            settingsFragment: SettingsFragment
        ): Lifecycle {
            return settingsFragment.viewLifecycleOwner.lifecycle
        }
    }
}
