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
     * The [block] receives an [OcrQueryScope] as its receiver — call filter methods directly
     * without any chaining or terminal call.
     */
    fun find(block: OcrQueryScope.() -> Unit): OcrLine? = OcrQueryScope(model.allLines).apply(block).find()
}
