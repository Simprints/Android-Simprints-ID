package com.simprints.feature.externalcredential.screens.scanocr.reader

/**
 * Defines the search criteria for locating a single line of text within a scanned document.
 * Used as the receiver of the [OcrReader.find] block.
 */
internal class OcrQuery {
    internal val filters = mutableListOf<(OcrLine) -> Boolean>()
    internal var belowAnchor: OcrQuery? = null
    internal var aboveAnchor: OcrQuery? = null

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
        belowAnchor = OcrQuery().apply(resolveAnchor)
    }

    fun isBelow(anchor: OcrLine): OcrQuery = apply {
        belowAnchor = OcrQuery().apply { filters += { line -> line.id == anchor.id } }
    }

    fun isAbove(resolveAnchor: OcrQuery.() -> Unit): OcrQuery = apply {
        aboveAnchor = OcrQuery().apply(resolveAnchor)
    }

    fun isAbove(anchor: OcrLine): OcrQuery = apply {
        aboveAnchor = OcrQuery().apply { filters += { line -> line.id == anchor.id } }
    }
}
