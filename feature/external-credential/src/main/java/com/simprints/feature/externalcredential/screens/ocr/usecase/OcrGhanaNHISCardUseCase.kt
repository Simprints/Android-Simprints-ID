package com.simprints.feature.externalcredential.screens.ocr.usecase

import androidx.core.text.isDigitsOnly
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.TextBlock
import com.simprints.feature.externalcredential.screens.ocr.model.OcrId
import com.simprints.feature.externalcredential.screens.ocr.tools.replaceCharactersWithDigits
import javax.inject.Inject

internal class OcrGhanaNHISCardUseCase @Inject constructor(
    private val ocrFuzzySearchUseCase: OcrFuzzySearchUseCase,
) {

    private val MEMBERSHIP_ID_LENGTH = 8

    operator fun invoke(ocrResult: Text): Map<OcrId, String?> {
        val ocrCredentialIdFuzzy =
            OcrId.FuzzySearch(fieldOnTheDocument = "MEMBERSHIP NO.", name = "membership", isExternalCredentialId = true)
        return when (val exactMatch = findViaExactMatch(ocrResult)) {
            null -> findWithFuzzySearch(ocrResult, ocrCredentialIdFuzzy)
            else -> {
                val ocrCredentialIdPattern = OcrId.Pattern(regex = "", fieldOnTheDocument = "MEMBERSHIP NO.", isExternalCredentialId = true)
                mapOf(ocrCredentialIdPattern to exactMatch)
            }
        }

    }

    private fun findViaExactMatch(ocrResult: Text): String? {
        val match = ocrResult.textBlocks.flatMap { it.lines }.map {
            it.elements.joinToString { e -> e.text }.trim().replace(" ", "").replaceCharactersWithDigits()
        }.firstOrNull {
            it.isDigitsOnly() && it.length == MEMBERSHIP_ID_LENGTH
        }
        return match
    }

    private fun findWithFuzzySearch(ocrResult: Text, ocrCredentialId: OcrId.FuzzySearch): Map<OcrId, String?> {
        val fieldIds = listOf(
            OcrId.FuzzySearch(fieldOnTheDocument = "NAME", name = "name"), // Looking for 'NAME' value
            OcrId.FuzzySearch(fieldOnTheDocument = "DATE OF BIRTH", name = "birth"), // Looking for 'NAME' value
        ) + ocrCredentialId
        val filteredTextBlocks = filterTextBlocks(ocrResult)
        val fuzzySearchResult = ocrFuzzySearchUseCase(filteredTextBlocks, fieldIds)
        return fuzzySearchResult.mapValues {
            if (it.key.isExternalCredentialId) {
                it.value.replaceCharactersWithDigits()
            } else {
                it.value
            }
        }
    }


    /**
     * Ghana NHIS card has two 'membership' fields, and we need to remove the irrelevant text block so that fuzzy search can find
     * the correct one
     */
    private fun filterTextBlocks(ocrResult: Text): List<TextBlock> {
        val blocks = ocrResult.textBlocks.toMutableList()
        val matchingIndices = blocks.withIndex()
            .filter { it.value.text.contains("member", ignoreCase = true) }
            .map { it.index }

        // Remove all but the last matching block
        if (matchingIndices.size > 1) {
            val toRemove = matchingIndices.dropLast(1)
            toRemove.reversed().forEach { blocks.removeAt(it) }
        }
        return blocks
    }
}
