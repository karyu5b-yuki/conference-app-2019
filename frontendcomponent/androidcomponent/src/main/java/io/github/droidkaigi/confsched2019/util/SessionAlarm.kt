package io.github.droidkaigi.confsched2019.util

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.AlarmManagerCompat
import com.soywiz.klock.DateTimeSpan
import com.soywiz.klock.minutes
import io.github.droidkaigi.confsched2019.broadcastreceiver.NotificationBroadcastReceiver
import io.github.droidkaigi.confsched2019.model.Session
import io.github.droidkaigi.confsched2019.model.SpeechSession
import io.github.droidkaigi.confsched2019.model.ServiceSession
import io.github.droidkaigi.confsched2019.model.defaultLang
import io.github.droidkaigi.confsched2019.widget.component.R
import javax.inject.Inject

class SessionAlarm @Inject constructor(private val app: Application) {
    fun toggleRegister(session: Session) {
        if (session.isFavorited) {
            unregister(session)
        } else {
            register(session)
        }
    }

    private fun register(session: Session) {
        val time = session.startTime.unixMillisLong.minus(NOTIFICATION_TIME_BEFORE_START_MILLS)

        if (System.currentTimeMillis() < time) {
            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                time,
                createAlarmIntent(session)
            )
        }
    }

    private fun unregister(session: Session) {
        val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(createAlarmIntent(session))
    }

    private fun createAlarmIntent(session: Session): PendingIntent {
        val timezoneOffset = DateTimeSpan(hours = 9).timeSpan // FIXME Get from device setting
        val displaySTime = session.startTime.toOffset(timezoneOffset).format("HH:mm")
        val displayETime = session.endTime.toOffset(timezoneOffset).format("HH:mm")
        val sessionTitle = app.getString(
            R.string.notification_message_session_title,
            when (session) {
                is SpeechSession -> session.title.getByLang(defaultLang())
                is ServiceSession -> session.title.getByLang(defaultLang())
            }
        )
        val sessionStartTime = app.getString(
            R.string.notification_message_session_start_time,
            displaySTime,
            displayETime,
            session.room.name
        )
        val title: String
        val text: String
        // If you make this notification under Android N, the time and location will not be displayed.
        // So, under Android N, the session title is displayed in the title, the time and location are displayed in the text.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            title = app.getString(R.string.notification_title_session_start)
            text = sessionTitle + "\n" + sessionStartTime
        } else {
            title = sessionTitle
            text = sessionStartTime
        }
        val intent = NotificationBroadcastReceiver.createForFavoritedSessionStart(
            app,
            session.id,
            title,
            text
        )
        return PendingIntent.getBroadcast(
            app,
            session.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        private val NOTIFICATION_TIME_BEFORE_START_MILLS = 10.minutes.millisecondsLong
    }
}
