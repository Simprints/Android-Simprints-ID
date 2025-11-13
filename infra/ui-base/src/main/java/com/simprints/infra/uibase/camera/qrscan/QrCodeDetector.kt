package com.simprints.infra.uibase.camera.qrscan

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This is just an injectable wrapper around MLKit barcode analyzer",
)
class QrCodeDetector @AssistedInject constructor(
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
) {
    @AssistedFactory
    interface Factory {
        fun create(crashReportTag: LoggingConstants.CrashReportTag): QrCodeDetector
    }

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    suspend fun detectInImage(rawImage: RawImage): String? =
        detectInImage(InputImage.fromMediaImage(rawImage.image, rawImage.rotationDegrees))

    suspend fun detectInImage(image: InputImage): String? = try {
        scanner
            .process(image)
            .awaitTask()
            ?.firstOrNull { !it.rawValue.isNullOrEmpty() }
            ?.rawValue
    } catch (t: Throwable) {
        Simber.e("QR code processing failed", t, tag = crashReportTag)
        null
    }

    private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
        this
            .addOnSuccessListener(continuation::resumeSafely)
            .addOnFailureListener {
                continuation.resumeWithExceptionSafely(it)
            }.addOnCanceledListener { continuation.cancel() }
    }
}
