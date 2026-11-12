package com.simprints.feature.externalcredential.screens.scanocr.usecase

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.feature.externalcredential.model.toBoundingBox
import com.simprints.feature.externalcredential.screens.scanocr.reader.ScannedBarcode
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ExcludedFromGeneratedTestCoverageReports("Unable to mock Google ML Kit")
internal class ReadBarcodesFromImageUseCase @Inject constructor() {
    // Fayda cards carry the FAN in a Code 128 barcode. Created on first use, as only Fayda scans read barcodes.
    private val scanner by lazy {
        BarcodeScanning.getClient(
            BarcodeScannerOptions
                .Builder()
                .setBarcodeFormats(Barcode.FORMAT_CODE_128)
                .build(),
        )
    }

    /**
     * Returns the barcodes detected in [bitmap], or an empty list if detection fails (e.g. the Play Services barcode module is not
     * available yet), so that callers can fall back to OCR.
     */
    operator fun invoke(bitmap: Bitmap): List<ScannedBarcode> = try {
        val image = InputImage.fromBitmap(bitmap, 0)
        Tasks.await(scanner.process(image)).orEmpty().mapNotNull { barcode ->
            val value = barcode.rawValue ?: return@mapNotNull null
            val box = barcode.boundingBox ?: return@mapNotNull null
            ScannedBarcode(value = value, boundingBox = box.toBoundingBox())
        }
    } catch (e: Exception) {
        // Logged as info as it happens on every frame and is handled by falling back to OCR. Info logs are Crashlytics breadcrumbs
        // that don't include the throwable, hence adding the exception details to the message.
        Simber.i("Barcode detection failed: ${e.javaClass.simpleName}: ${e.message}", e, tag = MULTI_FACTOR_ID)
        emptyList()
    }
}
