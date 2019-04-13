package io.github.droidkaigi.confsched2019.model

data class SettingContents(
    val preferences: MutableMap<String, Boolean> = mutableMapOf(sessionTit to false, sessionUrl to false, eventHashtag to false, roomHashtag to false)
) {
    companion object {
        private const val sessionTit = "session_title_key"
        private const val sessionUrl = "session_url_key"
        private const val eventHashtag = "event_hashtag_key"
        private const val roomHashtag = "room_hashtag_key"
    }
}
