package com.simprints.feature.externalcredential.screens.ocr.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class OcrParams(
    val ocrDocument: OcrDocument
) : Parcelable

@Keep
@Parcelize
sealed class OcrDocument : Parcelable {
    data object GhanaIdCard : OcrDocument()
    data object GhanaNHISCard : OcrDocument()
    data class Custom(
        /**
         * Specifies the ID fields to extract from the external credential card.
         * For example, if you need to extract name and surname from the card, you can pass the 'name' and 'surname' strings as
         * [OcrId.FuzzySearch] params, and the OCR will try to find these fields using fuzzy search and extract the text beneath such fields.
         * The field names are not case-sensitive and don't have to match the entire field exactly (i.e. 'urnam' is fine for the 'SURNAME' field)
         *
         * In case you want to extract a specific text using a Regex pattern, wrap it with the [OcrId.Pattern]. For instance, the Ghana ID card
         * has a syntax of 'GHA-12345678-9' for its person ID number, and it can be expressed as 'GHA-\d{9}-\d' regex.
         */
        val fieldIds: List<OcrId>
    ) : OcrDocument()
}

@Keep
@Parcelize
sealed class OcrId : Parcelable {
    /**
     * Field as it is written on the document (i.e.: 'Name', 'DOB', 'Personal ID')
     */
    abstract val fieldOnTheDocument: String

    /**
     * True if should be used as an external credential in 'Search & Verify', false otherwise
     */
    abstract val isExternalCredentialId: Boolean

    /**
     * Use this for a fuzzy search by field name. I.e: 'surname', 'personal ID', 'membership' etc.
     */
    data class FuzzySearch(
        val name: String,
        override val fieldOnTheDocument: String,
        override val isExternalCredentialId: Boolean = false,
    ) : OcrId()

    /**
     * Use this for a regex patter search. It will use the regex to find the line that best fits. I.e.: 'GHA-*' will take a line that starts
     * with 'GHA-'.
     */
    data class Pattern(
        val regex: String,
        override val fieldOnTheDocument: String,
        override val isExternalCredentialId: Boolean = false,
    ) : OcrId()
}
