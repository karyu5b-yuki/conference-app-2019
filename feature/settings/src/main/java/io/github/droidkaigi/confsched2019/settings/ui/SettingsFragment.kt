package io.github.droidkaigi.confsched2019.settings.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.droidkaigi.confsched2019.settings.R
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import io.github.droidkaigi.confsched2019.action.Action
import io.github.droidkaigi.confsched2019.system.actioncreator.PreferenceActionCreator
import javax.inject.Inject

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var preferenceActionCreator: PreferenceActionCreator

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate() {
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        SettingsStore.settingsResult.changed(viewLifecycleOwner) {
            // TODO 流れてきたら更新 xmlに記載しているswitchのidを取得して、bindする。
            // 対応するswitchのon offを変更する。
           /* settingsResult[]
            "@string/session_title_key",checkboxのon/offによるbool値
            "@string/session_url_key",
            "@string/event_hashtag_key",
            "@string/room_hashtag_key"*/
        }
    }

    override fun onDestory() {
        // TODO: ここで、現状のswitchのon, offをSharedPreferenceに保存してあげる。
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val changed = sharedPreferences?.getBoolean(
            key, false
        )
        // TODO: SettingContentsChangedをインスタンス化し、submitする。
        // 以下を行う際には、既存の4つの値のうち、変更されたものだけを更新して配列をコンストラクタの引数として渡す。
        // SettingContentsChanged(listof(true, true, true, false))みたいな感じ。実際は変数で置く。
        // NOTE: 実際にpreferenceActionCreator.submit()で行なっている処理は、dispatcherがactionを伝えること。
        preferenceActionCreator.submit(Action.SettingContentsChanged(mutableListOf()))

    }

}
