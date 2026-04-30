package com.simprints.feature.externalcredential.screens.scanocr.reader

/**
 * Defines the search criteria for locating a single line of text within a scanned document.
 * Used as the receiver of the [OcrReader.find] block.
 */
internal class OcrQuery(
    private val subQuery: (OcrQuery) -> OcrLine?,
) {
    internal val filters = mutableListOf<(OcrLine) -> Boolean>()
    internal var belowResolver: (() -> OcrLine?)? = null
    internal var aboveResolver: (() -> OcrLine?)? = null

    fun matchesPattern(regex: Regex): OcrQuery = apply {
        filters += { line -> regex.matches(line.text) }
    }

    fun containsPattern(regex: Regex): OcrQuery = apply {
        filters += { line -> regex.containsMatchIn(line.text) }
    }

    fun containsText(text: String): OcrQuery = apply {
        filters += { line -> line.text.contains(text, ignoreCase = true) }
    }

    fun hasExactText(text: String): OcrQuery = apply {
        filters += { line -> line.text.equals(text, ignoreCase = true) }
    }

    fun hasId(id: Int): OcrQuery = apply {
        filters += { line -> line.id == id }
    }

    fun isBelow(resolveAnchor: OcrQuery.() -> Unit): OcrQuery = apply {
        belowResolver = { subQuery(OcrQuery(subQuery).apply(resolveAnchor)) }
    }

    fun isBelow(anchor: OcrLine): OcrQuery = apply {
        belowResolver = { anchor }
    }

    fun isAbove(resolveAnchor: OcrQuery.() -> Unit): OcrQuery = apply {
        aboveResolver = { subQuery(OcrQuery(subQuery).apply(resolveAnchor)) }
    }

    fun isAbove(anchor: OcrLine): OcrQuery = apply {
        aboveResolver = { anchor }
    }
}
