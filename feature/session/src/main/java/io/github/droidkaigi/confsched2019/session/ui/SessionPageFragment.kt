package io.github.droidkaigi.confsched2019.session.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.shopify.livedataktx.observe
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import io.github.droidkaigi.confsched2019.ext.changed
import io.github.droidkaigi.confsched2019.model.AudienceCategory
import io.github.droidkaigi.confsched2019.model.Category
import io.github.droidkaigi.confsched2019.model.Lang
import io.github.droidkaigi.confsched2019.model.LangSupport
import io.github.droidkaigi.confsched2019.model.Room
import io.github.droidkaigi.confsched2019.model.SessionPage
import io.github.droidkaigi.confsched2019.model.defaultLang
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.databinding.FragmentSessionPageBinding
import io.github.droidkaigi.confsched2019.session.di.SessionAssistedInjectModule
import io.github.droidkaigi.confsched2019.session.di.SessionPageScope
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SessionContentsActionCreator
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SessionPageActionCreator
import io.github.droidkaigi.confsched2019.session.ui.actioncreator.SessionPagesActionCreator
import io.github.droidkaigi.confsched2019.session.ui.store.SessionContentsStore
import io.github.droidkaigi.confsched2019.session.ui.store.SessionPageStore
import io.github.droidkaigi.confsched2019.session.ui.store.SessionPagesStore
import io.github.droidkaigi.confsched2019.session.ui.widget.AudienceCategoryTagAttributes
import io.github.droidkaigi.confsched2019.session.ui.widget.CategoryTagAttributes
import io.github.droidkaigi.confsched2019.session.ui.widget.DaggerFragment
import io.github.droidkaigi.confsched2019.session.ui.widget.LangSupportTagAttributes
import io.github.droidkaigi.confsched2019.session.ui.widget.LangTagAttributes
import io.github.droidkaigi.confsched2019.session.ui.widget.RoomTagAttributes
import io.github.droidkaigi.confsched2019.session.ui.widget.SessionTagAttributes
import io.github.droidkaigi.confsched2019.widget.BottomSheetBehavior
import io.github.droidkaigi.confsched2019.widget.FilterChip
import io.github.droidkaigi.confsched2019.widget.onCheckedChanged
import me.tatarka.injectedvmprovider.InjectedViewModelProviders
import me.tatarka.injectedvmprovider.ktx.injectedViewModelProvider
import javax.inject.Inject
import javax.inject.Provider

class SessionPageFragment : DaggerFragment() {
    private lateinit var binding: FragmentSessionPageBinding

    @Inject lateinit var sessionContentsActionCreator: SessionContentsActionCreator
    @Inject lateinit var sessionPagesActionCreator: SessionPagesActionCreator
    @Inject lateinit var sessionPageActionCreator: SessionPageActionCreator
    @Inject lateinit var sessionStore: SessionContentsStore
    @Inject lateinit var sessionPageStoreFactory: SessionPageStore.Factory
    @Inject lateinit var sessionPagesStoreProvider: Provider<SessionPagesStore>
    val sessionPagesStore: SessionPagesStore by lazy {
        InjectedViewModelProviders.of(requireActivity())[sessionPagesStoreProvider]
    }

    private val sessionPageStore: SessionPageStore by lazy {
        injectedViewModelProvider
            .get(SessionPageStore::class.java.name) {
                sessionPageStoreFactory.create(SessionPage.pages[args.tabIndex])
            }
    }

    private val onPreDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            binding.sessionsSheet.viewTreeObserver.removeOnPreDrawListener(this)
            if (isDetached) return false
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

            return false
        }
    }

    private val args: SessionPageFragmentArgs by lazy {
        SessionPageFragmentArgs.fromBundle(arguments ?: Bundle())
    }

    private val bottomSheetBehavior: BottomSheetBehavior<*>
        get() = BottomSheetBehavior.from(binding.sessionsSheet)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            setupSessionsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_session_page, container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupBottomSheetBehavior()

        binding.sessionsFilterReset.setOnClickListener {
            sessionPagesActionCreator.clearFilters()
        }

        sessionPagesStore.filters.observe(viewLifecycleOwner) {
            applyFilters()
        }
        sessionStore.sessionContents.changed(viewLifecycleOwner) { contents ->
            val overrideTypeface = if (defaultLang() == Lang.JA) {
                ResourcesCompat.getFont(requireContext(), R.font.notosans_regular)
            } else {
                null
            }
            binding.sessionsFilterRoomChip.setupFilter(
                contents.rooms,
                RoomTagAttributes(requireContext()),
                null // Always use default typeface for room chips
            )
            binding.sessionsFilterCategoryChip.setupFilter(
                contents.category,
                CategoryTagAttributes(requireContext()),
                overrideTypeface
            )
            binding.sessionsFilterLangChip.setupFilter(
                contents.langs,
                LangTagAttributes(requireContext()),
                overrideTypeface
            )
            binding.sessionsFilterLangSupportChip.setupFilter(
                contents.langSupports,
                LangSupportTagAttributes(requireContext()),
                overrideTypeface
            )
            binding.sessionsFilterAudienceCategoryChip.setupFilter(
                contents.audienceCategories,
                AudienceCategoryTagAttributes(requireContext()),
                overrideTypeface
            )
        }
        sessionPagesStore.selectedTab.changed(viewLifecycleOwner) {
            if (SessionPage.pages[args.tabIndex] == it) {
                bottomSheetBehavior.isHideable = false
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        sessionPageStore.toggleFilterSheet.observe(viewLifecycleOwner) {
            bottomSheetBehavior.state =
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    BottomSheetBehavior.STATE_EXPANDED
                } else {
                    BottomSheetBehavior.STATE_COLLAPSED
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        teardownBottomSheetBehavior()
    }

    private fun setupSessionsFragment() {
        val tab = SessionPage.pages[args.tabIndex]
        val fragment: Fragment = when (tab) {
            is SessionPage.Day -> {
                BottomSheetDaySessionsFragment.newInstance(
                    BottomSheetDaySessionsFragmentArgs
                        .Builder(tab.day)
                        .build()
                )
            }
            SessionPage.Favorite -> {
                BottomSheetFavoriteSessionsFragment.newInstance()
            }
        }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.sessions_sheet, fragment, tab.title)
            .disallowAddToBackStack()
            .commit()
    }

    private fun setupBottomSheetBehavior() {
        bottomSheetBehavior.isHideable = false
        binding.sessionsSheet.viewTreeObserver.addOnPreDrawListener(onPreDrawListener)
        bottomSheetBehavior.addBottomSheetCallback(
            object : BottomSheetBehavior.BottomSheetCallback {
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    sessionPageActionCreator.changeFilterSheet(
                        SessionPage.pages[args.tabIndex], newState
                    )
                }
            }
        )
    }

    private fun teardownBottomSheetBehavior() {
        binding.sessionsSheet.viewTreeObserver.removeOnPreDrawListener(onPreDrawListener)
    }

    private fun <T> ChipGroup.setupFilter(
        items: List<T>,
        itemAttributes: SessionTagAttributes<T>,
        overrideTypeface: Typeface?
    ) {
        children.filterIsInstance<Chip>().forEach { it.setOnCheckedChangeListener(null) }
        removeAllViews()
        items
            .map { item ->
                val chip = LayoutInflater.from(context).inflate(
                    R.layout.layout_chip,
                    this,
                    false
                ) as FilterChip
                chip.apply {
                    text = itemAttributes.getText(item)
                    color = itemAttributes.getBackgroundColor(item)
                    textColor = itemAttributes.getTextColor(item)
                    selectedTextColor = itemAttributes.getTextColor(item)
                    tag = item
                    if (overrideTypeface != null) {
                        typeface = overrideTypeface
                    }
                }
            }
            .forEach {
                addView(it)
            }
        applyFilters()
    }

    private fun applyFilters() {
        val filterRooms = sessionPagesStore.filtersValue.rooms
        binding.sessionsFilterRoomChip.forEach {
            val chip = it as? FilterChip ?: return@forEach
            val room = it.tag as? Room ?: return@forEach
            chip.onCheckedChangeListener = null
            if (filterRooms.isNotEmpty()) {
                chip.isChecked = filterRooms.contains(room)
            } else {
                chip.isChecked = false
            }
            chip.onCheckedChanged { _, isChecked ->
                sessionPagesActionCreator.changeFilter(room, isChecked)
            }
        }
        val filterCategorys = sessionPagesStore.filtersValue.categories
        binding.sessionsFilterCategoryChip.forEach {
            val chip = it as? FilterChip ?: return@forEach
            val category = it.tag as? Category ?: return@forEach
            chip.onCheckedChangeListener = null
            if (filterCategorys.isNotEmpty()) {
                chip.isChecked = filterCategorys.contains(category)
            } else {
                chip.isChecked = false
            }
            chip.onCheckedChanged { _, isChecked ->
                sessionPagesActionCreator.changeFilter(category, isChecked)
            }
        }
        val filterLangs = sessionPagesStore.filtersValue.langs
        binding.sessionsFilterLangChip.forEach {
            val chip = it as? FilterChip ?: return@forEach
            val lang = it.tag as? Lang ?: return@forEach
            chip.onCheckedChangeListener = null
            if (filterLangs.isNotEmpty()) {
                chip.isChecked = filterLangs.contains(lang)
            } else {
                chip.isChecked = false
            }
            chip.onCheckedChanged { _, isChecked ->
                sessionPagesActionCreator.changeFilter(lang, isChecked)
            }
        }
        val filterLangSupports = sessionPagesStore.filtersValue.langSupports
        binding.sessionsFilterLangSupportChip.forEach {
            val chip = it as? FilterChip ?: return@forEach
            val langSupport = it.tag as? LangSupport ?: return@forEach
            chip.onCheckedChangeListener = null
            if (filterLangSupports.isNotEmpty()) {
                chip.isChecked = filterLangSupports.contains(langSupport)
            } else {
                chip.isChecked = false
            }
            chip.onCheckedChanged { _, isChecked ->
                sessionPagesActionCreator.changeFilter(langSupport, isChecked)
            }
        }
        val filterAudienceCategories = sessionPagesStore.filtersValue.audienceCategories
        binding.sessionsFilterAudienceCategoryChip.forEach {
            val chip = it as? FilterChip ?: return@forEach
            val audienceCategory = it.tag as? AudienceCategory ?: return@forEach
            chip.onCheckedChangeListener = null
            if (filterAudienceCategories.isNotEmpty()) {
                chip.isChecked = filterAudienceCategories.contains(audienceCategory)
            } else {
                chip.isChecked = false
            }
            chip.onCheckedChanged { _, isChecked ->
                sessionPagesActionCreator.changeFilter(audienceCategory, isChecked)
            }
        }
    }

    companion object {
        fun newInstance(args: SessionPageFragmentArgs): SessionPageFragment {
            return SessionPageFragment().apply {
                arguments = args.toBundle()
            }
        }
    }
}

@Module
abstract class SessionPageFragmentModule {

    @ContributesAndroidInjector(
        modules = [SessionAssistedInjectModule::class]
    )
    abstract fun contributeBottomSheetDaySessionsFragment(): BottomSheetDaySessionsFragment

    @ContributesAndroidInjector(
        modules = [SessionAssistedInjectModule::class]
    )
    abstract fun contributeFavoriteSessionsFragment(): BottomSheetFavoriteSessionsFragment

    @Module
    companion object {
        @JvmStatic @SessionPageScope @Provides fun providesLifecycle(
            sessionPageFragment: SessionPageFragment
        ): LifecycleOwner {
            return sessionPageFragment.viewLifecycleOwner
        }
    }
}
