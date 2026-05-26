package com.simprints.feature.externalcredential.screens.scanocr.reader

import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.domain.step.StepParams
import com.simprints.feature.externalcredential.model.BoundingBox
import kotlinx.serialization.Serializable

/**
 * Wrapper for all scanned text after the OCR
 *
 * @param allLines all lines from blocks sorted by bounding box top coordinate ascending
 */
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class OcrText(
    val allLines: List<OcrLine>,
) : StepParams

/**
 * A single line of text detected by the OCR kit.
 *
 * @param id unique id of the line in a single OCR scan
 * @param text normalized text (extra spaces removed)
 * @param boundingBox coordinates of the line
 * @param blockBoundingBox parent coordinates
 * @param confidence overall confidence of the text value based on the average confidence for each character (aka element) in [text]
 */
@Serializable
@ExcludedFromGeneratedTestCoverageReports("Data class")
internal data class OcrLine(
    val id: Int,
    val text: String,
    val boundingBox: BoundingBox,
    val blockBoundingBox: BoundingBox,
    val confidence: Float,
) : StepParams
