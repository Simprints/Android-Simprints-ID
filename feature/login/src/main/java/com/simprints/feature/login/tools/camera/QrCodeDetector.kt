package com.simprints.feature.login.tools.camera

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extentions.resumeSafely
import com.simprints.core.tools.extentions.resumeWithExceptionSafely
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LOGIN
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

@ExcludedFromGeneratedTestCoverageReports(
    reason = "This is just an injectable wrapper around MLKit barcode analyzer",
)
internal class QrCodeDetector @Inject constructor() {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    suspend fun detectInImage(rawImage: RawImage): String? = try {
        scanner
            .process(InputImage.fromMediaImage(rawImage.image, rawImage.rotationDegrees))
            .awaitTask()
            ?.firstOrNull { !it.rawValue.isNullOrEmpty() }
            ?.rawValue
    } catch (t: Throwable) {
        Simber.e("QR code processing failed", t, tag = LOGIN)
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
