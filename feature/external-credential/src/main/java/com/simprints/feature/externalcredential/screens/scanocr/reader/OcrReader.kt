package com.simprints.feature.externalcredential.screens.scanocr.reader

/**
 * Entry point for querying an [OcrText].
 *
 * Usage:
 * ```
 * val reader = OcrReader(ocrText)
 *
 * val membershipNumber = reader.find {
 *     matchesPattern(Regex("\\d{8}"))
 *     isBelow { containsText("membership number") }
 *     isAbove { containsText("expiry date") }
 * }
 * ```
 */
internal class OcrReader(
    val ocrText: OcrText,
) {
    /**
     * Executes the query defined in [block] and returns the first matching [OcrLine], or null.
     * The [block] receives an [OcrQuery] as its receiver — call filter methods directly
     * without any chaining or terminal call.
     *
     * Usage:
     * ```
     * val reader = OcrReader(ocrText)
     *
     * val membershipNumber = reader.find {
     *     matchesPattern(Regex("\\d{8}"))
     *     isBelow { containsText("membership number") }
     *     isAbove { containsText("expiry date") }
     * }
     * ```
     *
     * Only one level of nesting is supported. Spatial filters inside an anchor block are silently ignored.
     * The following is NOT supported:
     * ```
     * reader.find {
     *     isBelow {
     *         isBelow { containsText("some major title") }  // ignored — has no effect
     *         containsText("some subtitle")
     *     }
     * }
     * ```
     */
    fun find(block: OcrQuery.() -> Unit): OcrLine? = runQuery(OcrQuery().apply(block))

    private fun runQuery(query: OcrQuery): OcrLine? {
        val belowAnchor = query.belowAnchor?.let { runQuery(it) }
        val aboveAnchor = query.aboveAnchor?.let { runQuery(it) }

        if (query.belowAnchor != null && belowAnchor == null) return null
        if (query.aboveAnchor != null && aboveAnchor == null) return null

        return ocrText.allLines.firstOrNull { line ->
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
            query.filters.all { it(line) } && isBelowAnchor && isAboveAnchor
        }
    }
}
