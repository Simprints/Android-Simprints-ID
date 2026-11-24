package com.simprints.feature.externalcredential.screens.scanocr.model

/**
 * A single successful readout of a document: what was read and which scanner read it.
 */
internal data class ReadoutResult(
    val source: ReadoutSource,
    val scanResult: OcrScanResult,
)
