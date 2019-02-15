package io.github.droidkaigi.confsched2019.model

sealed class Message {
    class ResourceIdMessage(val messageId: Int, val stringArgs: Array<out Any>) : Message()
    class TextMessage(val message: String) : Message()

    companion object {
        fun of(messageId: Int, vararg arg: Any): Message =
            ResourceIdMessage(messageId, arg)

        fun of(message: String): Message =
            TextMessage(message)
    }
}
