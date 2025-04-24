package com.simprints.feature.externalcredential.screens.ocr.usecase

import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import javax.inject.Inject
import kotlin.math.ceil

internal class OcrFuzzySearchUseCase @Inject constructor() {

    operator fun invoke(ocrResult: Text, fieldIDs: List<OcrId.FuzzySearch>): Map<OcrId, String?> = invoke(ocrResult.textBlocks, fieldIDs)

    operator fun invoke(textBlocks: List<TextBlock>, fieldIDs: List<OcrId.FuzzySearch>): Map<OcrId, String?> {
        val result: MutableMap<OcrId, String> = fieldIDs.associateWith { "" }.toMutableMap()
        val linesFromOCR = textBlocks.flatMap { it.lines }.map { it.text }

        fieldIDs.forEach { fieldId ->
            val index = linesFromOCR.indexOfFirst { line ->
                return@indexOfFirst line.contains(fieldId.name, ignoreCase = true)
                    || line.contains(fieldId.fieldOnTheDocument, ignoreCase = true)
                    || isCloseEnough(
                    line.lowercase(),
                    fieldId.name.lowercase(),
                    tolerance = 0.2f
                ) // 20% threshold is allowed to be different
            }

            if (index != -1 && index + 1 < linesFromOCR.size) {
                result[fieldId] = linesFromOCR[index + 1].trim()
            }
        }

        return result
    }

    private fun isCloseEnough(text: String, target: String, tolerance: Float): Boolean {
        val maxAllowed = ceil(target.length * tolerance).toInt()

        if (text.length < target.length) return levenshtein(text, target) <= maxAllowed

        for (i in 0..(text.length - target.length)) {
            val window = text.substring(i, i + target.length)
            val distance = levenshtein(window, target)
            if (distance <= maxAllowed) return true
        }

        return false
    }

    private fun levenshtein(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j

        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }

        return dp[a.length][b.length]
    }
}
