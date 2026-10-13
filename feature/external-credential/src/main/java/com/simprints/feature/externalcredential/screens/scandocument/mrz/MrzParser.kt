package com.simprints.feature.externalcredential.screens.scandocument.mrz

import javax.inject.Inject

/**
 * Parser of the ICAO 9303 Machine Readable Zone from OCR output. Supports TD1 (ID cards, 3x30) and TD3 (passports, 2x44) layouts.
 *
 * OCR output is noisy, so the parser:
 *  - normalises the lines (case, whitespace, look-alike filler characters) and pads up to [MAX_MISSING_FILLERS] dropped trailing fillers
 *  - fixes common letter/digit confusions based on the field type (numeric vs alphabetic)
 *  - tries the letter/digit swaps in the alphanumeric document number, accepting only a single unambiguous variant
 *  - accepts the MRZ only when every check digit (including the composite one) is valid
 */
internal class MrzParser @Inject constructor() {
    /**
     * @param ocrLines OCR text lines sorted top to bottom
     * @return parsed MRZ, or null if no valid MRZ was found
     */
    fun parse(ocrLines: List<String>): MrzData? {
        val candidates = ocrLines
            .map(::normalise)
            .filter { line -> line.length >= TD1_LINE_LENGTH - MAX_MISSING_FILLERS && line.all(::isMrzChar) }

        for (start in candidates.indices) {
            val window = candidates.drop(start)
            parseTd3(window)?.let { return it }
            parseTd1(window)?.let { return it }
        }
        return null
    }

    private fun parseTd3(lines: List<String>): MrzData? {
        if (lines.size < 2) return null
        val line1 = lines[0].fitTo(TD3_LINE_LENGTH) ?: return null
        val line2 = lines[1].fitTo(TD3_LINE_LENGTH) ?: return null
        if (!line1[0].isLetter()) return null

        val dateOfBirth = line2.substring(13, 19).asNumeric() ?: return null
        val dateOfBirthCheck = line2[19].asNumeric() ?: return null
        val dateOfExpiry = line2.substring(21, 27).asNumeric() ?: return null
        val dateOfExpiryCheck = line2[27].asNumeric() ?: return null
        val optionalData = line2.substring(28, 42)
        val optionalDataCheck = line2[42].let { if (it == FILLER) it else it.asNumeric() } ?: return null
        val compositeCheck = line2[43].asNumeric() ?: return null
        val documentNumberCheck = line2[9].asNumeric() ?: return null

        if (!isCheckDigitValid(dateOfBirth, dateOfBirthCheck)) return null
        if (!isCheckDigitValid(dateOfExpiry, dateOfExpiryCheck)) return null
        if (!isOptionalDataCheckDigitValid(optionalData, optionalDataCheck)) return null

        val documentNumberField = resolveDocumentNumberField(line2.substring(0, 9), documentNumberCheck) { candidate ->
            val composite = candidate + documentNumberCheck + dateOfBirth + dateOfBirthCheck +
                dateOfExpiry + dateOfExpiryCheck + optionalData + optionalDataCheck
            isCheckDigitValid(composite, compositeCheck)
        } ?: return null

        val (primaryIdentifier, secondaryIdentifier) = parseNames(line1.substring(5))
        return MrzData(
            format = MrzFormat.TD3,
            documentCode = line1.substring(0, 2).asAlphabetic() ?: return null,
            issuingState = line1.substring(2, 5).asAlphabetic() ?: return null,
            documentNumber = documentNumberField.trimFillers(),
            dateOfBirth = dateOfBirth,
            sex = line2[20].asSex() ?: return null,
            dateOfExpiry = dateOfExpiry,
            nationality = line2.substring(10, 13).asAlphabetic() ?: return null,
            primaryIdentifier = primaryIdentifier,
            secondaryIdentifier = secondaryIdentifier,
            optionalData1 = optionalData.trimFillers(),
            optionalData2 = "",
        )
    }

    private fun parseTd1(lines: List<String>): MrzData? {
        if (lines.size < 2) return null
        val line1 = lines[0].fitTo(TD1_LINE_LENGTH) ?: return null
        val line2 = lines[1].fitTo(TD1_LINE_LENGTH) ?: return null
        // The 3rd line contains only the names and is not covered by check digits, so it is optional
        val line3 = lines.getOrNull(2)?.fitTo(TD1_LINE_LENGTH)
        if (!line1[0].isLetter()) return null

        val dateOfBirth = line2.substring(0, 6).asNumeric() ?: return null
        val dateOfBirthCheck = line2[6].asNumeric() ?: return null
        val dateOfExpiry = line2.substring(8, 14).asNumeric() ?: return null
        val dateOfExpiryCheck = line2[14].asNumeric() ?: return null
        val optionalData2 = line2.substring(18, 29)
        val compositeCheck = line2[29].asNumeric() ?: return null

        if (!isCheckDigitValid(dateOfBirth, dateOfBirthCheck)) return null
        if (!isCheckDigitValid(dateOfExpiry, dateOfExpiryCheck)) return null

        val compositeSuffix = dateOfBirth + dateOfBirthCheck + dateOfExpiry + dateOfExpiryCheck + optionalData2
        val documentNumber: String
        val optionalData1: String
        if (line1[14] == FILLER) {
            // Extended document number (more than 9 characters): continues in the optional data, followed by its check digit
            val optionalData = line1.substring(15, 30)
            val extension = optionalData.substringBefore(FILLER)
            if (extension.length < 2) return null
            documentNumber = line1.substring(5, 14) + extension.dropLast(1)
            val documentNumberCheck = extension.last().asNumeric() ?: return null
            if (!isCheckDigitValid(documentNumber, documentNumberCheck)) return null
            if (!isCheckDigitValid(line1.substring(5, 30) + compositeSuffix, compositeCheck)) return null
            optionalData1 = optionalData.substring(extension.length).trimFillers()
        } else {
            val documentNumberCheck = line1[14].asNumeric() ?: return null
            val optionalData = line1.substring(15, 30)
            val documentNumberField = resolveDocumentNumberField(line1.substring(5, 14), documentNumberCheck) { candidate ->
                isCheckDigitValid(candidate + documentNumberCheck + optionalData + compositeSuffix, compositeCheck)
            } ?: return null
            documentNumber = documentNumberField.trimFillers()
            optionalData1 = optionalData.trimFillers()
        }

        val (primaryIdentifier, secondaryIdentifier) = line3?.let(::parseNames) ?: ("" to "")
        return MrzData(
            format = MrzFormat.TD1,
            documentCode = line1.substring(0, 2).asAlphabetic() ?: return null,
            issuingState = line1.substring(2, 5).asAlphabetic() ?: return null,
            documentNumber = documentNumber,
            dateOfBirth = dateOfBirth,
            sex = line2[7].asSex() ?: return null,
            dateOfExpiry = dateOfExpiry,
            nationality = line2.substring(15, 18).asAlphabetic() ?: return null,
            primaryIdentifier = primaryIdentifier,
            secondaryIdentifier = secondaryIdentifier,
            optionalData1 = optionalData1,
            optionalData2 = optionalData2.trimFillers(),
        )
    }

    /**
     * Document number is alphanumeric, so letter/digit OCR confusions cannot be fixed by field type. Instead, letter/digit swaps are
     * tried, starting with the fewest swaps (OCR usually misreads a single character). The field is accepted only if exactly one variant
     * with the smallest number of swaps satisfies both its own and the composite check digit.
     */
    private fun resolveDocumentNumberField(
        field: String,
        checkDigit: Char,
        isCompositeValid: (String) -> Boolean,
    ): String? {
        val isValid = { candidate: String -> isCheckDigitValid(candidate, checkDigit) && isCompositeValid(candidate) }
        if (isValid(field)) return field

        val ambiguousPositions = field.indices.filter { field[it] in DOCUMENT_NUMBER_SWAPS }
        if (ambiguousPositions.isEmpty() || ambiguousPositions.size > MAX_AMBIGUOUS_POSITIONS) return null

        val variantsBySwapCount = (1 until (1 shl ambiguousPositions.size)).groupBy(Int::countOneBits).toSortedMap()
        for (masks in variantsBySwapCount.values) {
            val validVariants = masks
                .map { mask ->
                    val chars = field.toCharArray()
                    ambiguousPositions.forEachIndexed { bit, position ->
                        if (mask and (1 shl bit) != 0) chars[position] = DOCUMENT_NUMBER_SWAPS.getValue(chars[position])
                    }
                    String(chars)
                }.filter(isValid)
            if (validVariants.isNotEmpty()) return validVariants.singleOrNull()
        }
        return null
    }

    private fun isOptionalDataCheckDigitValid(
        optionalData: String,
        checkDigit: Char,
    ) = if (checkDigit == FILLER) {
        optionalData.all { it == FILLER }
    } else {
        isCheckDigitValid(optionalData, checkDigit)
    }

    private fun parseNames(nameField: String): Pair<String, String> {
        val primary = nameField.substringBefore("$FILLER$FILLER")
        val secondary = nameField.substringAfter("$FILLER$FILLER", missingDelimiterValue = "")
        return primary.fillersToSpaces() to secondary.fillersToSpaces()
    }

    private fun normalise(line: String) = line
        .uppercase()
        .filterNot(Char::isWhitespace)
        .map { if (it in FILLER_LOOKALIKES) FILLER else it }
        .joinToString("")

    private fun String.fitTo(length: Int): String? = when (this.length) {
        length -> this
        in (length - MAX_MISSING_FILLERS) until length -> padEnd(length, FILLER)
        else -> null
    }

    private fun String.asNumeric(): String? = map { it.asNumeric() ?: return null }.joinToString("")

    private fun Char.asNumeric(): Char? = (NUMERIC_FIXES[this] ?: this).takeIf(Char::isDigit)

    private fun String.asAlphabetic(): String? = map { char ->
        (ALPHABETIC_FIXES[char] ?: char).takeIf { it in 'A'..'Z' || it == FILLER } ?: return null
    }.joinToString("")

    private fun Char.asSex(): String? = (ALPHABETIC_FIXES[this] ?: this).takeIf { it in "MFX$FILLER" }?.toString()

    private fun String.trimFillers() = trim(FILLER)

    private fun String.fillersToSpaces() = split(FILLER).filter(String::isNotEmpty).joinToString(" ")

    private fun isMrzChar(char: Char) = char in 'A'..'Z' || char in '0'..'9' || char == FILLER

    companion object {
        const val FILLER = '<'
        private const val TD1_LINE_LENGTH = 30
        private const val TD3_LINE_LENGTH = 44

        /** OCR frequently drops trailing fillers. Missing ones are restored, any error this might introduce is caught by check digits */
        private const val MAX_MISSING_FILLERS = 3

        /** Caps the number of document number variants (2^n) to try */
        private const val MAX_AMBIGUOUS_POSITIONS = 6

        private val FILLER_LOOKALIKES = setOf('«', '‹', '≤', '(', '{', '[')
        private val NUMERIC_FIXES = mapOf(
            'O' to '0',
            'Q' to '0',
            'D' to '0',
            'I' to '1',
            'L' to '1',
            'Z' to '2',
            'S' to '5',
            'G' to '6',
            'B' to '8',
        )
        private val ALPHABETIC_FIXES = mapOf('0' to 'O', '1' to 'I', '2' to 'Z', '5' to 'S', '6' to 'G', '8' to 'B')
        private val DOCUMENT_NUMBER_SWAPS = mapOf(
            '0' to 'O',
            'O' to '0',
            '1' to 'I',
            'I' to '1',
            '2' to 'Z',
            'Z' to '2',
            '5' to 'S',
            'S' to '5',
            '8' to 'B',
            'B' to '8',
        )
        private val CHECK_DIGIT_WEIGHTS = intArrayOf(7, 3, 1)

        /** ICAO 9303 document number: alphanumeric, up to 9 characters, or longer when using the TD1 extended format */
        val DOCUMENT_NUMBER_PATTERN = Regex("^[A-Z0-9]{1,22}$")

        /** ICAO 9303 part 3, section 4.9 */
        fun checkDigit(value: String): Char {
            val sum = value.foldIndexed(0) { index, acc, char ->
                val charValue = when (char) {
                    in '0'..'9' -> char - '0'
                    in 'A'..'Z' -> char - 'A' + 10
                    else -> 0 // filler
                }
                acc + charValue * CHECK_DIGIT_WEIGHTS[index % CHECK_DIGIT_WEIGHTS.size]
            }
            return '0' + sum % 10
        }

        private fun isCheckDigitValid(
            value: String,
            checkDigit: Char,
        ) = checkDigit(value) == checkDigit
    }
}
