package io.github.droidkaigi.confsched2019.settings.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.droidkaigi.confsched2019.settings.R
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable





class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onResume() {
        super.onResume()

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()

        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        val changedValue = sharedPreferences?.getBoolean(
            key, false
        )
//処理をここに書こう。
    }
}

protected fun onGetDefaultValue(a: TypedArray, index: Int): Any {
    return a.getInteger(index, DEFAULT_VALUE)
}

private class SavedState : BaseSavedState {
    // Member that holds the setting's value
    // Change this data type to match the type saved by your Preference
    internal var value: Int = 0

    constructor(superState: Parcelable) : super(superState) {}

    constructor(source: Parcel) : super(source) {
        // Get the current preference's value
        value = source.readInt()  // Change this to read the appropriate data type
    }

    fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        // Write the preference's value
        dest.writeInt(value)  // Change this to write the appropriate data type
    }

    companion object {

        // Standard creator object using an instance of this class
        val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

            override fun createFromParcel(`in`: Parcel): SavedState {
                return SavedState(`in`)
            }

            override fun newArray(size: Int): Array<SavedState> {
                return arrayOfNulls(size)
            }
        }
    }
}
