package io.github.droidkaigi.confsched2019.model

import platform.Foundation.NSLocale
import platform.Foundation.preferredLanguages

actual fun defaultLang() = if (NSLocale.preferredLanguages.first().toString().startsWith("ja")) Lang.JA else Lang.EN
