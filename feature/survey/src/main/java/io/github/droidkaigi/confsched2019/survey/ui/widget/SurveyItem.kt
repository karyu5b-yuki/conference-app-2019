package io.github.droidkaigi.confsched2019.survey.ui.widget

import android.content.Context
import androidx.annotation.StringRes
import io.github.droidkaigi.confsched2019.survey.R

enum class SurveyItem(
    val position: Int,
    @StringRes private val titleResId: Int,
    val isComment: Boolean
) {
    TOTAL_EVALUATION(
        position = 0,
        titleResId = R.string.session_survey_total_evaluation,
        isComment = false
    ),

    RELEVANCY(
        position = 1,
        titleResId = R.string.session_survey_relevancy,
        isComment = false
    ),
    AS_EXPECTED(
        position = 2,
        titleResId = R.string.session_survey_as_expected,
        isComment = false
    ),
    DIFFICULTY(
        position = 3,
        titleResId = R.string.session_survey_difficulty,
        isComment = false
    ),
    KNOWLEDGEABLE(
        position = 4,
        titleResId = R.string.session_survey_knowledgeable,
        isComment = false
    ),
    COMMENT(
        position = 5,
        titleResId = R.string.session_survey_comment,
        isComment = true
    );

    fun title(context: Context): String = context.getString(titleResId)

    companion object {
        fun of(position: Int): SurveyItem {
            return values().first { it.position == position }
        }
    }
}
