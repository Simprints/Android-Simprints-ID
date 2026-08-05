package com.simprints.infra.camera.postprocess

import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.simprints.core.DispatcherBG
import com.simprints.core.ExcludedFromGeneratedTestCoverageReports
import com.simprints.core.tools.extensions.resumeSafely
import com.simprints.core.tools.extensions.resumeWithExceptionSafely
import com.simprints.infra.camera.Frame
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.Simber
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

@ExcludedFromGeneratedTestCoverageReports(
    reason = "Wrapper around QR detection provided by Play Services ML Kit",
)
class DetectQrCodeUseCase @AssistedInject constructor(
    @param:DispatcherBG private val bgDispatcher: CoroutineDispatcher,
    @Assisted private val crashReportTag: LoggingConstants.CrashReportTag,
) {
    @AssistedFactory
    interface Factory {
        fun create(crashReportTag: LoggingConstants.CrashReportTag): DetectQrCodeUseCase
    }

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions
            .Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build(),
    )

    suspend operator fun invoke(frame: Frame): String? = withContext(bgDispatcher) {
        try {
            detectInImage(InputImage.fromBitmap(frame.bitmap, frame.rotation))
        } catch (t: Throwable) {
            Simber.e("QR code detection failed", t, tag = crashReportTag)
            null
        }
    }

    private suspend fun detectInImage(image: InputImage): String? = try {
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
            .addOnFailureListener { continuation.resumeWithExceptionSafely(it) }
            .addOnCanceledListener { continuation.cancel() }
    }
}
