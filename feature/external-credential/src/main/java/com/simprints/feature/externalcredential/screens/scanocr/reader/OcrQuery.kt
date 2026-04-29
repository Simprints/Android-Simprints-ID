package com.simprints.feature.externalcredential.screens.scanocr.reader

/**
 * Defines the search criteria for locating a single line of text within document scanned with OCR.
 */
internal class OcrQueryScope internal constructor(
    internal val allLines: List<OcrLine>,
) {
    private val filters = mutableListOf<(OcrLine) -> Boolean>()
    private var belowResolver: (() -> OcrLine?)? = null
    private var aboveResolver: (() -> OcrLine?)? = null

    fun matchesPattern(regex: Regex): OcrQueryScope = apply {
        filters += { line -> regex.containsMatchIn(line.text) }
    }

    fun containsText(text: String): OcrQueryScope = apply {
        filters += { line -> line.text.contains(text, ignoreCase = true) }
    }

    fun hasExactText(text: String): OcrQueryScope = apply {
        filters += { line -> line.text.equals(text, ignoreCase = true) }
    }

    fun hasId(id: Int): OcrQueryScope = apply {
        filters += { line -> line.id == id }
    }

    fun isBelow(resolveAnchor: OcrQueryScope.() -> Unit): OcrQueryScope = apply {
        belowResolver = { OcrQueryScope(allLines).apply(resolveAnchor).find() }
    }

    fun isBelow(anchor: OcrLine): OcrQueryScope = apply {
        belowResolver = { anchor }
    }

    fun isAbove(resolveAnchor: OcrQueryScope.() -> Unit): OcrQueryScope = apply {
        aboveResolver = { OcrQueryScope(allLines).apply(resolveAnchor).find() }
    }

    fun isAbove(anchor: OcrLine): OcrQueryScope = apply {
        aboveResolver = { anchor }
    }

    internal fun find(): OcrLine? {
        val belowAnchor = belowResolver?.invoke()
        val aboveAnchor = aboveResolver?.invoke()

        if (belowResolver != null && belowAnchor == null) return null
        if (aboveResolver != null && aboveAnchor == null) return null

        return allLines.firstOrNull { line ->
            val isBelowAnchor = belowAnchor == null || (
                line.boundingBox.top > belowAnchor.boundingBox.top &&
                    line.boundingBox.left >= belowAnchor.boundingBox.left &&
                    line.boundingBox.left < belowAnchor.boundingBox.right
            )
            val isAboveAnchor = aboveAnchor == null || (
                line.boundingBox.top < aboveAnchor.boundingBox.top &&
                    line.boundingBox.left >= aboveAnchor.boundingBox.left &&
                    line.boundingBox.left < aboveAnchor.boundingBox.right
            )
            filters.all { it(line) } && isBelowAnchor && isAboveAnchor
        }
    }
}
