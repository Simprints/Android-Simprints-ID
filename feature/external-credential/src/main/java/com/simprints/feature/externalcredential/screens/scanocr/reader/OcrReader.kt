package com.simprints.feature.externalcredential.screens.scanocr.reader

/**
 * Entry point for querying an [OcrText].
 *
 * Usage:
 * ```
 * val model = OcrModelBuilder.build(mlKitText)
 * val reader = OcrReader(model)
 *
 * val membershipNumber = reader.find {
 *     matchesPattern(Regex("\\d{8}"))
 *     isBelow { containsText("membership number") }
 *     isAbove { containsText("expiry date") }
 * }
 * ```
 */
internal class OcrReader(
    val model: OcrText,
) {
    /**
     * Executes the query defined in [block] and returns the first matching [OcrLine], or null.
     * The [block] receives an [OcrQuery] as its receiver — call filter methods directly
     * without any chaining or terminal call.
     */
    fun find(block: OcrQuery.() -> Unit): OcrLine? = runQuery(OcrQuery(::runQuery).apply(block))

    private fun runQuery(scope: OcrQuery): OcrLine? {
        val belowAnchor = scope.belowResolver?.invoke()
        val aboveAnchor = scope.aboveResolver?.invoke()

        if (scope.belowResolver != null && belowAnchor == null) return null
        if (scope.aboveResolver != null && aboveAnchor == null) return null

        return model.allLines.firstOrNull { line ->
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
            scope.filters.all { it(line) } && isBelowAnchor && isAboveAnchor
        }
    }
}
