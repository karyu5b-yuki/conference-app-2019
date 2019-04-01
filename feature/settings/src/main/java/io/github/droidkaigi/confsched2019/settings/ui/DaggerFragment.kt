package io.github.droidkaigi.confsched2019.settings.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject

open class DaggerFragment : PreferenceFragmentCompat(), HasSupportFragmentInjector{

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    }

    @Inject lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onActivityCreated(savedInstanceState)
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return childFragmentInjector
    }
}
