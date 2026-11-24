package com.simprints.feature.externalcredential.screens.scanocr.model

import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test

internal class MfidCaptureStateTest {
    @Test
    fun `fayda with barcode enabled requires 1 barcode or 3 ocr scans`() {
        val state = MfidCaptureState(OcrDocumentType.FaydaCard, config(readFaydaBarcode = true))

        assertThat(state.maxScansRequired).isEqualTo(3)
        state.add(document(ReadoutSource.BARCODE))
        assertThat(state.isComplete).isTrue()
    }

    @Test
    fun `fayda with barcode disabled requires ocr scans only`() {
        val state = MfidCaptureState(OcrDocumentType.FaydaCard, config(readFaydaBarcode = false))

        state.add(document(ReadoutSource.BARCODE))
        assertThat(state.isComplete).isFalse()

        repeat(3) { state.add(document(ReadoutSource.TEXT_OCR)) }
        assertThat(state.isComplete).isTrue()
    }

    @Test
    fun `ghana cards require ocr scans only`() {
        listOf(OcrDocumentType.NhisCard, OcrDocumentType.GhanaIdCard).forEach { type ->
            val state = MfidCaptureState(type, config(readFaydaBarcode = true))

            assertThat(state.maxScansRequired).isEqualTo(3)
            state.add(document(ReadoutSource.BARCODE))
            assertThat(state.isComplete).isFalse()
        }
    }

    @Test
    fun `ocr scans complete after required count`() {
        val state = MfidCaptureState(OcrDocumentType.FaydaCard, config(readFaydaBarcode = true))

        repeat(2) { state.add(document(ReadoutSource.TEXT_OCR)) }
        assertThat(state.isComplete).isFalse()

        state.add(document(ReadoutSource.TEXT_OCR))
        assertThat(state.isComplete).isTrue()
        assertThat(state.documents).hasSize(3)
    }

    @Test
    fun `barcode after ocr scans completes and keeps all documents`() {
        val state = MfidCaptureState(OcrDocumentType.FaydaCard, config(readFaydaBarcode = true))

        repeat(2) { state.add(document(ReadoutSource.TEXT_OCR)) }
        state.add(document(ReadoutSource.BARCODE))

        assertThat(state.isComplete).isTrue()
        assertThat(state.documents).hasSize(3)
    }

    @Test
    fun `documents after completion are ignored`() {
        val state = MfidCaptureState(OcrDocumentType.FaydaCard, config(readFaydaBarcode = true))

        state.add(document(ReadoutSource.BARCODE))
        state.add(document(ReadoutSource.TEXT_OCR))

        assertThat(state.documents).hasSize(1)
    }

    private fun config(readFaydaBarcode: Boolean) = OcrConfig(
        useHighRes = false,
        keepHighResDetail = false,
        capturesRequired = 3,
        readFaydaBarcode = readFaydaBarcode,
    )

    private fun document(source: ReadoutSource) = ScannedMfidDocument(
        imagePath = "path",
        ocrScanResult = OcrScanResult.FaydaCard(credential = mockk()),
        source = source,
    )
}
