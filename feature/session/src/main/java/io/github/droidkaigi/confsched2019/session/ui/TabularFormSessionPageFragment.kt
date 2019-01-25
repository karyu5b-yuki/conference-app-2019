package io.github.droidkaigi.confsched2019.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.databinding.ViewHolder
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.ext.android.changed
import io.github.droidkaigi.confsched2019.model.ServiceSession
import io.github.droidkaigi.confsched2019.model.Session
import io.github.droidkaigi.confsched2019.model.SpeechSession
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.databinding.FragmentTabularFormSessionPageBinding
import io.github.droidkaigi.confsched2019.session.di.SessionPageScope
import io.github.droidkaigi.confsched2019.session.ui.item.TabularServiceSessionItem
import io.github.droidkaigi.confsched2019.session.ui.item.TabularSpacerItem
import io.github.droidkaigi.confsched2019.session.ui.item.TabularSpeechSessionItem
import io.github.droidkaigi.confsched2019.session.ui.store.SessionPagesStore
import io.github.droidkaigi.confsched2019.session.ui.widget.DaggerFragment
import io.github.droidkaigi.confsched2019.session.ui.widget.TimeTableDividerDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimeTableLayoutManager
import io.github.droidkaigi.confsched2019.session.ui.widget.TimeTableRoomLabelDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimeTableTimeLabelDecoration
import me.tatarka.injectedvmprovider.InjectedViewModelProviders
import javax.inject.Inject
import javax.inject.Provider

class TabularFormSessionPageFragment : DaggerFragment() {

    private lateinit var binding: FragmentTabularFormSessionPageBinding

    @Inject lateinit var sessionPagesStoreProvider: Provider<SessionPagesStore>
    private val sessionPagesStore: SessionPagesStore by lazy {
        InjectedViewModelProviders.of(requireActivity()).get(sessionPagesStoreProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tabular_form_session_page,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val groupAdapter = GroupAdapter<ViewHolder<*>>()
        binding.tabularFormSessionsRecycler.apply {
            addItemDecoration(TimeTableDividerDecoration(context, COLUMN_COUNT, groupAdapter))
            addItemDecoration(TimeTableTimeLabelDecoration(context, groupAdapter))
            addItemDecoration(TimeTableRoomLabelDecoration(context, groupAdapter))
            layoutManager = TimeTableLayoutManager(
                resources.getDimensionPixelSize(R.dimen.tabular_form_column_width),
                resources.getDimensionPixelSize(R.dimen.tabular_form_px_per_minute)
            ) { position ->
                val item = groupAdapter.getItem(position)
                when (item) {
                    is TabularSpeechSessionItem ->
                        TimeTableLayoutManager.PeriodInfo(
                            item.session.startTime.unixMillisLong,
                            item.session.endTime.unixMillisLong,
                            item.session.room.sequentialNumber
                        )
                    is TabularServiceSessionItem ->
                        TimeTableLayoutManager.PeriodInfo(
                            item.session.startTime.unixMillisLong,
                            item.session.endTime.unixMillisLong,
                            item.session.room.sequentialNumber
                        )
                    is TabularSpacerItem ->
                        TimeTableLayoutManager.PeriodInfo(
                            item.startUnixMillis,
                            item.endUnixMillis,
                            item.room.sequentialNumber
                        )
                    else -> TimeTableLayoutManager.PeriodInfo(0, 0, 0)
                }
            }
            adapter = groupAdapter
        }

        sessionPagesStore.sessionsByDay(arguments?.getInt(KEY_DAY) ?: 1) // TODO: SafeArgs
            .changed(viewLifecycleOwner) { sessions ->
                groupAdapter.update(fillGaps(sessions))
            }
    }

    private fun fillGaps(sessions: List<Session>): List<BindableItem<*>> {
        if (sessions.isEmpty()) return emptyList()

        val sortedSessions = sessions.sortedBy { it.startTime.unixMillisLong }
        val firstSessionStart = sortedSessions.first().startTime.unixMillisLong
        val lastSessionEnd =
            sortedSessions.maxBy { it.endTime.unixMillisLong }?.endTime?.unixMillisLong
                ?: return emptyList()
        val rooms = sortedSessions.map { it.room }.distinct()

        val filledItems = ArrayList<BindableItem<*>>()
        rooms.forEach { room ->
            val sessionsInSameRoom = sortedSessions.filter { it.room == room }
            sessionsInSameRoom.forEachIndexed { index, session ->
                if (index == 0 && session.startTime.unixMillisLong > firstSessionStart)
                    filledItems.add(
                        TabularSpacerItem(
                            firstSessionStart,
                            session.startTime.unixMillisLong,
                            room
                        )
                    )

                filledItems.add(
                    when (session) {
                        is SpeechSession -> TabularSpeechSessionItem(session)
                        is ServiceSession -> TabularServiceSessionItem(session)
                    }
                )

                if (index == sessionsInSameRoom.size - 1 &&
                    session.endTime.unixMillisLong < lastSessionEnd
                ) {
                    filledItems.add(
                        TabularSpacerItem(
                            session.endTime.unixMillisLong,
                            lastSessionEnd,
                            room
                        )
                    )
                }

                val nextSession = sessionsInSameRoom.getOrNull(index + 1) ?: return@forEachIndexed
                if (session.endTime.unixMillisLong != nextSession.startTime.unixMillisLong)
                    filledItems.add(
                        TabularSpacerItem(
                            session.endTime.unixMillisLong,
                            nextSession.startTime.unixMillisLong,
                            room
                        )
                    )
            }
        }
        return filledItems.sortedBy {
            when (it) {
                is TabularSpeechSessionItem -> it.session.startTime.unixMillisLong
                is TabularServiceSessionItem -> it.session.startTime.unixMillisLong
                is TabularSpacerItem -> it.startUnixMillis
                else -> 0
            }
        }
    }

    companion object {
        // TODO: use SageArgs
        const val COLUMN_COUNT = 9
        const val KEY_DAY = "day"

        fun newInstance(day: Int): TabularFormSessionPageFragment {
            return TabularFormSessionPageFragment()
                .apply { arguments = Bundle().apply { putInt(KEY_DAY, day) } }
        }
    }
}

@Module
abstract class TabularFormSessionPageFragmentModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @SessionPageScope
        fun providesLifecycle(
            tabularFromSessionPageFragment: TabularFormSessionPageFragment
        ): Lifecycle {
            return tabularFromSessionPageFragment.viewLifecycleOwner.lifecycle
        }
    }
}
