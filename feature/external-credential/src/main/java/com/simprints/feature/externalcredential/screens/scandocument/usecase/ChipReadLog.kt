package com.simprints.feature.externalcredential.screens.scandocument.usecase

import net.sf.scuba.smartcards.CardServiceException
import java.util.Locale

/**
 * PoC: short log of a single chip read attempt (status updates, failure reasons and a summary of what was read), displayed on screen.
 *
 * Every occurrence of the [sensitiveValues] (the access key components) is redacted. The log is kept in memory only and is never sent
 * to the logging infrastructure.
 */
internal class ChipReadLog(
    sensitiveValues: Collection<String>,
) {
    private val sensitiveValues = sensitiveValues.filter { it.length >= MIN_REDACTED_LENGTH }.sortedByDescending { it.length }
    private val entries = mutableListOf<String>()
    private var lastLoggedFailure: Throwable? = null

    val lines: List<String>
        get() = entries.toList()

    fun add(message: String) {
        entries += redact(message)
    }

    /** Runs the [block] and, if it fails, logs the failure reason. The exception is rethrown. */
    fun <T> step(
        name: String,
        block: () -> T,
    ): T = try {
        block()
    } catch (e: Throwable) {
        addFailure(name, e)
        throw e
    }

    /** Logs the failure reason, unless this exception has already been logged by a [step] */
    fun addFailure(
        name: String,
        throwable: Throwable,
    ) {
        if (throwable === lastLoggedFailure) return
        lastLoggedFailure = throwable
        add("$name failed: ${failureReason(throwable)}")
    }

    /** The chip status word if there is one, as it is the most specific reason, otherwise the root cause */
    private fun failureReason(throwable: Throwable): String {
        val causes = generateSequence(throwable) { it.cause }.take(MAX_CAUSES).toList()
        val statusWord = causes
            .filterIsInstance<CardServiceException>()
            .map { it.sw }
            .firstOrNull { it != CardServiceException.SW_NONE }
        if (statusWord != null) {
            return String.format(Locale.ROOT, "%s (SW %04X)", describeStatusWord(statusWord), statusWord and 0xFFFF)
        }
        val rootCause = causes.last()
        return listOfNotNull(rootCause.javaClass.simpleName, rootCause.message).joinToString(": ")
    }

    private fun redact(text: String) = sensitiveValues.fold(text) { acc, value -> acc.replace(value, REDACTED, ignoreCase = true) }

    companion object {
        private const val MIN_REDACTED_LENGTH = 4
        private const val MAX_CAUSES = 6
        const val REDACTED = "<redacted>"

        /** ISO 7816-4 and ICAO 9303 status words that are relevant when reading an eMRTD */
        fun describeStatusWord(sw: Int): String = when (sw and 0xFFFF) {
            0x6282 -> "end of file reached early"
            0x6300 -> "wrong access key"
            0x6700 -> "wrong length"
            0x6882 -> "secure messaging not supported"
            0x6982 -> "access not granted"
            0x6983 -> "authentication blocked"
            0x6985 -> "conditions of use not satisfied"
            0x6987 -> "secure messaging data missing"
            0x6988 -> "secure messaging session out of sync"
            0x6A80 -> "incorrect data"
            0x6A82 -> "file not found"
            0x6A86, 0x6B00 -> "incorrect parameters"
            0x6A88 -> "referenced data not found"
            0x6D00 -> "instruction not supported"
            0x6E00 -> "class not supported"
            else -> "chip error"
        }
    }
}
