package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.feature.externalcredential.screens.scanocr.model.OcrScanResult
import com.simprints.feature.externalcredential.screens.scanocr.reader.OcrReader
import javax.inject.Inject

internal class FaydaCardOcrReaderUseCase @Inject constructor() {
    operator fun invoke(ocrReader: OcrReader): OcrScanResult.FaydaCard? = ocrReader.ocrText.allLines.firstNotNullOfOrNull { line ->
        FAN_OCR_PATTERN.find(line.text)?.let { match ->
            OcrScanResult.FaydaCard(line.copy(text = match.value.filter(Char::isDigit)))
        }
    }

    companion object {
        // Fayda Alias Number (FAN): exactly 16 digits
        val FAN_PATTERN = Regex("^\\d{16}$")

        // FAN within an OCR line: 16 digits, optionally split by OCR into up to 4 groups separated by 1-2 whitespace characters,
        // and not part of a longer digit run (also across 1-2 whitespace characters). Wider gaps are treated as separate numbers.
        // Punctuation between digits and more than 4 groups are not allowed, so that adjacent dates such as
        // "07/02/1998 | 2005/10/17" are not read as a FAN, even if OCR reads the separators as spaces ("07 02 1998 2005 10 17").
        val FAN_OCR_PATTERN = Regex("(?<!\\d\\s{0,2})(?=(?:\\d+\\s{1,2}){0,3}\\d+(?!\\s{0,2}\\d))\\d(?:\\s{0,2}\\d){15}(?!\\s{0,2}\\d)")
    }
}
