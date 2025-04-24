package com.simprints.feature.externalcredential.screens.ocr.tools

/**
 * Some IDs only contains digits. We need to make sure that every character in a certain region is a digit. OCR might misread the digits
 * as look-alike characters, and this map tries to link the possible incorrect readouts to their real values
 */
private val charSubstitutions = mapOf(
    'o' to '0',
    'O' to '0',
    'i' to '1',
    'I' to '1',
    'l' to '1',
    'L' to '1',
    'Z' to '2',
    'z' to '2',
    'S' to '5',
    's' to '5',
    'B' to '8',
    'b' to '6',
    'G' to '6',
    'g' to '9',
    'q' to '9',
    'T' to '7'
)

fun String?.replaceCharactersWithDigits() = charSubstitutions.entries
    .fold(this ?: "") { acc, (from, to) ->
        acc.replace(from, to, ignoreCase = false) // trying to change characters to digits
    }.replace(Regex("[^\\d]"), "") // removing spaces and non-digits
    .trim()
