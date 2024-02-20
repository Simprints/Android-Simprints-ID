package com.simprints.feature.consent.screens.consent.helpers

internal fun StringBuilder.appendSentence(text: String): StringBuilder {
    if (isNotEmpty()) {
        append(" ")
    }
    append(text)
    return this
}
