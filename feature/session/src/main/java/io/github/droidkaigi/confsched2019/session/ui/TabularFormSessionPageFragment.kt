package io.github.droidkaigi.confsched2019.session.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.BindableItem
import com.xwray.groupie.databinding.ViewHolder
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.ext.Dispatchers
import io.github.droidkaigi.confsched2019.ext.changed
import io.github.droidkaigi.confsched2019.ext.coroutineScope
import io.github.droidkaigi.confsched2019.model.ServiceSession
import io.github.droidkaigi.confsched2019.model.Session
import io.github.droidkaigi.confsched2019.model.SessionType
import io.github.droidkaigi.confsched2019.model.SpeechSession
import io.github.droidkaigi.confsched2019.session.R
import io.github.droidkaigi.confsched2019.session.databinding.FragmentTabularFormSessionPageBinding
import io.github.droidkaigi.confsched2019.session.di.SessionPageScope
import io.github.droidkaigi.confsched2019.session.ui.item.TabularServiceSessionItem
import io.github.droidkaigi.confsched2019.session.ui.item.TabularSpacerItem
import io.github.droidkaigi.confsched2019.session.ui.item.TabularSpeechSessionItem
import io.github.droidkaigi.confsched2019.session.ui.store.SessionContentsStore
import io.github.droidkaigi.confsched2019.session.ui.store.SessionPagesStore
import io.github.droidkaigi.confsched2019.session.ui.widget.DaggerFragment
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableCurrentTimeLabelDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableCurrentTimeLineDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableLayoutManager
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableLunchDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableRoomLabelDecoration
import io.github.droidkaigi.confsched2019.session.ui.widget.TimetableTimeLabelDecoration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class TabularFormSessionPageFragment : DaggerFragment() {

    private lateinit var binding: FragmentTabularFormSessionPageBinding

    @Inject lateinit var tabularSpeechSessionItemFactory: TabularSpeechSessionItem.Factory
    @Inject lateinit var tabularServiceSessionItemFactory: TabularServiceSessionItem.Factory
    @Inject lateinit var sessionPagesStoreProvider: Provider<SessionPagesStore>
    @Inject lateinit var navController: NavController
    @Inject lateinit var sessionContentsStore: SessionContentsStore

    private lateinit var args: TabularFormSessionPagesFragmentArgs

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
        args = TabularFormSessionPagesFragmentArgs.fromBundle(arguments ?: Bundle())

        val groupAdapter = GroupAdapter<ViewHolder<*>>()
        binding.tabularFormSessionsRecycler.apply {
            val timetableCurrentTimeLabelDecoration =
                TimetableCurrentTimeLabelDecoration(context, groupAdapter)

            val timetableCurrentTimeLineDecoration =
                TimetableCurrentTimeLineDecoration(context, groupAdapter)

            addItemDecoration(TimetableTimeLabelDecoration(context, groupAdapter))
            addItemDecoration(TimetableRoomLabelDecoration(context, groupAdapter))
            addItemDecoration(TimetableLunchDecoration(context, groupAdapter))
            addItemDecoration(timetableCurrentTimeLabelDecoration)
            layoutManager = TimetableLayoutManager(
                resources.getDimensionPixelSize(R.dimen.tabular_form_column_width),
                resources.getDimensionPixelSize(R.dimen.tabular_form_px_per_minute)
            ) { position ->
                val item = groupAdapter.getItem(position)
                when (item) {
                    is TabularSpeechSessionItem ->
                        TimetableLayoutManager.PeriodInfo(
                            item.session.startTime.unixMillisLong,
                            item.session.endTime.unixMillisLong,
                            item.session.room.sequentialNumber
                        )
                    is TabularServiceSessionItem ->
                        TimetableLayoutManager.PeriodInfo(
                            item.session.startTime.unixMillisLong,
                            item.session.endTime.unixMillisLong,
                            item.session.room.sequentialNumber
                        )
                    is TabularSpacerItem ->
                        TimetableLayoutManager.PeriodInfo(
                            item.startUnixMillis,
                            item.endUnixMillis,
                            item.room.sequentialNumber
                        )
                    else -> TimetableLayoutManager.PeriodInfo(0, 0, 0)
                }
            }
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        val job = viewLifecycleOwner.coroutineScope.launch(Dispatchers.Main) {
                            delay(500)
                            removeItemDecoration(timetableCurrentTimeLabelDecoration)
                            removeItemDecoration(timetableCurrentTimeLineDecoration)
                            addItemDecoration(timetableCurrentTimeLineDecoration)
                        }
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            job.start()
                        } else {
                            if (job.isActive) { job.cancel() }
                            removeItemDecoration(timetableCurrentTimeLabelDecoration)
                            removeItemDecoration(timetableCurrentTimeLineDecoration)
                            addItemDecoration(timetableCurrentTimeLabelDecoration)
                        }
                    }
                }
            )
            adapter = groupAdapter
        }
        sessionContentsStore.sessionsByDay(args.day)
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

        // FIXME: Add lunch sessions for all rooms to get lunch item view.
        val lunchSession = sortedSessions.find {
            (it as? ServiceSession)?.sessionType == SessionType.LUNCH
        } as? ServiceSession

        val sortedSessionsWithLunch = sortedSessions + rooms
            .filter { it.id != lunchSession?.room?.id }
            .mapNotNull { lunchSession?.copy(room = it) }

        val filledItems = ArrayList<BindableItem<*>>()
        rooms.forEach { room ->
            val sessionsInSameRoom = sortedSessionsWithLunch
                .sortedBy { it.startTime.unixMillisLong }
                .filter { it.room == room }
            sessionsInSameRoom.forEachIndexed { index, session ->
                if (index == 0 && session.startTime.unixMillisLong > firstSessionStart)
                    filledItems.add(
                        TabularSpacerItem(
                            firstSessionStart,
                            session.startTime.unixMillisLong,
                            room
                        )
                    )
                val navDirections = TabularFormSessionPagesFragmentDirections
                    .actionTabularFormToSessionDetail(session.id)
                filledItems.add(
                    when (session) {
                        is SpeechSession ->
                            tabularSpeechSessionItemFactory.create(session, navDirections)
                        is ServiceSession ->
                            tabularServiceSessionItemFactory.create(session, navDirections)
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
        fun newInstance(args: TabularFormSessionPagesFragmentArgs): TabularFormSessionPageFragment {
            return TabularFormSessionPageFragment()
                .apply { arguments = args.toBundle() }
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
