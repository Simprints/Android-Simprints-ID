package com.simprints.infra.logging.writers

import co.touchlab.kermit.Message
import co.touchlab.kermit.MessageStringFormatter
import co.touchlab.kermit.Severity
import co.touchlab.kermit.Tag

internal class OptionalTagFormatter : MessageStringFormatter {
    override fun formatTag(tag: Tag): String = "[${tag.tag}]"

    override fun formatMessage(
        severity: Severity?,
        tag: Tag?,
        message: Message,
    ): String {
        if (tag?.tag.isNullOrBlank()) return message.message
        return "${formatTag(tag)} ${message.message}"
    }
}
