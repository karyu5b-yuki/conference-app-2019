package io.github.droidkaigi.confsched2019.survey.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.shopify.livedataktx.filter
import com.shopify.livedataktx.first
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.databinding.ViewHolder
import dagger.Module
import dagger.Provides
import io.github.droidkaigi.confsched2019.di.PageScope
import io.github.droidkaigi.confsched2019.ext.changed
import io.github.droidkaigi.confsched2019.ext.requireValue
import io.github.droidkaigi.confsched2019.model.SessionFeedback
import io.github.droidkaigi.confsched2019.model.defaultLang
import io.github.droidkaigi.confsched2019.survey.R
import io.github.droidkaigi.confsched2019.survey.databinding.FragmentSessionSurveyBinding
import io.github.droidkaigi.confsched2019.survey.databinding.ItemSessionSurveyBinding
import io.github.droidkaigi.confsched2019.survey.ui.actioncreator.SessionSurveyActionCreator
import io.github.droidkaigi.confsched2019.survey.ui.item.SpeakerIconItem
import io.github.droidkaigi.confsched2019.survey.ui.store.SessionSurveyStore
import io.github.droidkaigi.confsched2019.survey.ui.widget.DaggerFragment
import io.github.droidkaigi.confsched2019.survey.ui.widget.SurveyItem
import io.github.droidkaigi.confsched2019.util.ProgressTimeLatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.tatarka.injectedvmprovider.ktx.injectedViewModelProvider
import timber.log.Timber
import timber.log.debug
import javax.inject.Inject
import javax.inject.Provider

class SessionSurveyFragment : DaggerFragment() {

    @Inject lateinit var sessionSurveyActionCreator: SessionSurveyActionCreator
    @Inject lateinit var sessionSurveyStoreProvider: Provider<SessionSurveyStore>
    private val sessionSurveyStore: SessionSurveyStore by lazy {
        injectedViewModelProvider[sessionSurveyStoreProvider]
    }

    private lateinit var binding: FragmentSessionSurveyBinding
    private lateinit var progressTimeLatch: ProgressTimeLatch

    private lateinit var sessionSurveyFragmentArgs: SessionSurveyFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_session_survey,
            container,
            false
        )
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sessionSurveyFragmentArgs = SessionSurveyFragmentArgs.fromBundle(arguments ?: Bundle())

        val groupAdapter = GroupAdapter<ViewHolder<*>>()
        binding.speakerIcons.adapter = groupAdapter

        val speakerIconItems = sessionSurveyFragmentArgs.session.speakers.map {
            SpeakerIconItem(it)
        }
        groupAdapter.update(speakerIconItems)

        progressTimeLatch = ProgressTimeLatch { showProgress ->
            binding.progressBar.isVisible = showProgress
        }

        sessionSurveyStore.loadingState.changed(viewLifecycleOwner) {
            progressTimeLatch.loading = it.isLoading
            if (it.isInitialized) {
                // when error, we should re enable button
                binding.submitButton.isEnabled = true
            }
        }

        sessionSurveyStore.sessionFeedback.changed(viewLifecycleOwner) { sessionFeedback ->
            Timber.debug { sessionFeedback.toString() }
            applySubmitButton()

            // TODO: save sessionFeedback state to cacheDB
        }

        sessionSurveyStore.sessionFeedback
            .filter { it != SessionFeedback.EMPTY }
            .first()
            .changed(viewLifecycleOwner) {
                binding.sessionSurveyViewPager.adapter?.notifyDataSetChanged()
            }

        val lang = defaultLang()

        binding.sessionTitle.text = sessionSurveyFragmentArgs.session.title.getByLang(lang)

        binding.submitButton.setOnClickListener {
            val sessionFeedback =
                sessionSurveyStore.sessionFeedback.requireValue().copy(submitted = true)
            if (sessionFeedback.fillouted) {
                binding.submitButton.isEnabled = false
                // TODO: show confirm dialog
                sessionSurveyActionCreator.submit(
                    sessionSurveyFragmentArgs.session,
                    sessionFeedback
                )
            } else {
                sessionSurveyActionCreator.processMessage(R.string.session_survey_not_input)
            }
        }

        setupSessionSurveyPager()
        sessionSurveyActionCreator.load(sessionSurveyFragmentArgs.session.id)
    }

    private fun applySubmitButton() {
        val submitted = sessionSurveyStore.submitted
        binding.submitButton.isEnabled = !submitted
        val currentItem = binding.sessionSurveyViewPager.currentItem
        val isLastItem = currentItem == (enumValues<SurveyItem>().size - 1)
        binding.submitButton.isVisible = isLastItem || sessionSurveyStore.submitted
        val text = if (submitted) {
            R.string.session_survey_submit_end
        } else {
            R.string.session_survey_submit
        }
        binding.submitButton.setText(text)
        val iconRes = if (submitted) {
            R.drawable.ic_check_black_24dp
        } else {
            R.drawable.ic_baseline_send_24px
        }
        val drawable = ContextCompat.getDrawable(requireContext(), iconRes) ?: return
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        wrappedDrawable.setTint(ContextCompat.getColor(requireContext(), R.color.white))
        binding.submitButton.setCompoundDrawablesWithIntrinsicBounds(
            wrappedDrawable,
            null,
            null,
            null
        )
        val drawablePadding =
            resources.getDimensionPixelSize(R.dimen.session_survey_submit_drawable_padding)
        binding.submitButton.compoundDrawablePadding = drawablePadding
    }

    private fun setupSessionSurveyPager() {
        binding.sessionSurveyViewPager.adapter = object : PagerAdapter() {
            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val itemBinding = DataBindingUtil.inflate<ItemSessionSurveyBinding>(
                    LayoutInflater.from(context),
                    R.layout.item_session_survey,
                    container,
                    true
                )

                itemBinding.surveyItem = SurveyItem.of(position)

                if (position == SurveyItem.COMMENT.position) {
                    itemBinding.comment.setText(
                        sessionSurveyStore.comment,
                        TextView.BufferType.EDITABLE
                    )
                } else {
                    when (position) {
                        SurveyItem.TOTAL_EVALUATION.position -> {
                            itemBinding.rating.rating = sessionSurveyStore.totalEvaluation.toFloat()
                        }
                        SurveyItem.RELEVANCY.position -> {
                            itemBinding.rating.rating = sessionSurveyStore.relevancy.toFloat()
                        }
                        SurveyItem.AS_EXPECTED.position -> {
                            itemBinding.rating.rating = sessionSurveyStore.asExpected.toFloat()
                        }
                        SurveyItem.DIFFICULTY.position -> {
                            itemBinding.rating.rating = sessionSurveyStore.difficulty.toFloat()
                        }
                        SurveyItem.KNOWLEDGEABLE.position -> {
                            itemBinding.rating.rating = sessionSurveyStore.knowledgeable.toFloat()
                        }
                    }
                }

                itemBinding.rating.setOnRatingBarChangeListener { _, rating, fromUser ->
                    if (!fromUser) return@setOnRatingBarChangeListener

                    val newSessionFeedback = when (position) {
                        SurveyItem.TOTAL_EVALUATION.position -> {
                            sessionSurveyStore.sessionFeedback.requireValue()
                                .copy(totalEvaluation = rating.toInt())
                        }
                        SurveyItem.RELEVANCY.position -> {
                            sessionSurveyStore.sessionFeedback.requireValue()
                                .copy(relevancy = rating.toInt())
                        }
                        SurveyItem.AS_EXPECTED.position -> {
                            sessionSurveyStore.sessionFeedback.requireValue()
                                .copy(asExpected = rating.toInt())
                        }
                        SurveyItem.DIFFICULTY.position -> {
                            sessionSurveyStore.sessionFeedback.requireValue()
                                .copy(difficulty = rating.toInt())
                        }
                        SurveyItem.KNOWLEDGEABLE.position -> {
                            sessionSurveyStore.sessionFeedback.requireValue()
                                .copy(knowledgeable = rating.toInt())
                        }
                        else -> sessionSurveyStore.sessionFeedback.requireValue()
                    }

                    sessionSurveyActionCreator.changeSessionFeedback(newSessionFeedback)
                    GlobalScope.launch(Dispatchers.Main) {
                        // Late transition a little to improve the experience
                        delay(300)
                        binding.sessionSurveyViewPager.setCurrentItem(position + 1, true)
                    }
                }

                itemBinding.comment.onTextChange { text ->
                    val newSessionFeedback = sessionSurveyStore.sessionFeedback.requireValue()
                        .copy(comment = text)
                    sessionSurveyActionCreator.changeSessionFeedback(newSessionFeedback)
                }

                return itemBinding.root
            }

            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
                container.removeView(`object` as View)
            }

            override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

            override fun getCount(): Int = enumValues<SurveyItem>().size

            // views should be recreated on PagerAdapter#notifyDataSetChanged
            override fun getItemPosition(`object`: Any): Int = POSITION_NONE
        }

        binding.sessionSurveyViewPager.onPageSelected { position ->
            binding.pageProgress.text = getString(
                R.string.session_survey_page_progress,
                position + 1,
                (binding.sessionSurveyViewPager.adapter as PagerAdapter).count
            )
            applySubmitButton()
        }
    }
}

private fun EditText.onTextChange(onChanged: (text: String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) = Unit
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onChanged(text.toString())
        }
    })
}

private fun ViewPager.onPageSelected(onPageSelected: (position: Int) -> Unit) {
    this.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) = Unit
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) = Unit

        override fun onPageSelected(position: Int) {
            onPageSelected(position)
        }
    })
}

@Module
abstract class SessionSurveyFragmentModule {

    @Module
    companion object {
        @JvmStatic
        @Provides
        @PageScope
        fun providesLifecycle(sessionSurveyFragment: SessionSurveyFragment): Lifecycle {
            return sessionSurveyFragment.viewLifecycleOwner.lifecycle
        }
    }
}
