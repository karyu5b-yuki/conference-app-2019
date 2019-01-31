package io.github.droidkaigi.confsched2019.settings.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.droidkaigi.confsched2019.settings.R
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject lateinit var preferenceActionCreator: PreferenceActionCreator

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreate() {
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        SettingsStore.settingsResult.changed(viewLifecycleOwner) { settingsResult ->
            // TODO 流れてきたら更新 xmlに記載しているswitchのidを取得して、bindする。
            // 対応するswitchのon offを変更する。
        }
    }

    override fun onDestory() {
        // TODO: ここで、現状のswitchのon, offをSharedPreferenceに保存してあげる。
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val changedValue = sharedPreferences?.getBoolean(
            key, false
        )
        // TODO: SettingContentsChangedをインスタンス化し、submitする。
        // 以下を行う際には、既存の4つの値のうち、変更されたものだけを更新して配列をコンストラクタの引数として渡す。
        // SettingContentsChanged(listof(true, true, true, false))みたいな感じ。実際は変数で置く。
        // NOTE: 実際にpreferenceActionCreator.submit()で行なっている処理は、dispatcherがactionを伝えること。
        preferenceActionCreator.submit(SettingContentsChanged())
        //処理をここに書こう。
    }

}
