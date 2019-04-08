package io.github.droidkaigi.confsched2019.settings.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.lifecycle.Lifecycle
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.ext.changed
import io.github.droidkaigi.confsched2019.settings.R
import javax.inject.Inject

class SettingsFragment : DaggerFragment(),
    setContentView(R.layout.preferences)
    val preference = findViewById<CheckBox>(R.id.__)
    checkbox.setOnClickListener{

}
    SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject lateinit var preferenceActionCreator: PreferenceActionCreator
    @Inject lateinit var settingsStore: SettingsStore

//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.frament, container, false)
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsStore.settingsResult.changed(viewLifecycleOwner) { settingContents ->
            for (content in settingContents.preferences) {
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putBoolean(content.key, content.value ?: false)
                editor.apply()
            }

            Log.d("settingContents", settingContents.toString())
        }

        // TODO 流れてきたら更新 xmlに記載しているswitchのidを取得して、bindする。
        /* val preferenceList = <Listをとる何か>
           val switches = [各switchのidを調べて、switchの配列を作る]
           for i in 0..3 {
             if(preferenceList[i]) {
                switches[i].switchOn
             } else {
                switches[i].switchOff
             }
           }
         */
        // 対応するswitchのon offを変更する。
//        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        // TODO: ここで、現状のswitchのon, offをSharedPreferenceに保存してあげる。
//        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val contents = mutableMapOf<String, Boolean?>()
        val changed_title = sharedPreferences?.getBoolean(
            "session_title", false
        )
        val changed_url = sharedPreferences?.getBoolean(
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

        // TODO: SettingContentsChangedをインスタンス化し、submitする。
        // 以下を行う際には、既存の4つの値のうち、変更されたものだけを更新して配列をコンストラクタの引数として渡す。
        // SettingContentsChanged(listof(true, true, true, false))みたいな感じ。実際は変数で置く。
        // NOTE: 実際にpreferenceActionCreator.submit()で行なっている処理は、dispatcherがactionを伝えること。
        preferenceActionCreator.submit(Action.SettingContentsChanged(contents))
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
/*
    private fun settingContent(
        changed_title: Boolean?,
        changed_url: Boolean?,
        changed_event: Boolean?,
        changed_room: Boolean?
    ): MutableMap<Any, Any> =
        mutableMapOf(changed_title, changed_url, changed_event, changed_room)
}

    preferenceActionCreator.submit(Action.SettingContentsChanged(
    -            settingContent(
    -                changed_title,
    -                changed_url,
    -                changed_event,
    -                changed_room
    -            )
    -        ))
*/
