package com.simprints.feature.externalcredential.screens.scanocr.usecase

import com.simprints.core.DispatcherIO
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class DeleteScannedImageUseCase @Inject constructor(
    @DispatcherIO private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Deletes a file from the given absolute path.
     * Only deletes files within the application's cache directory for security.
     * @param filePath the absolute path to the file to delete
     * @return true if the file was successfully deleted, false otherwise
     */
    suspend operator fun invoke(filePath: String) {
        withContext(ioDispatcher) {
            try {
                val file = File(filePath)
                if (file.exists() && file.isFile) {
                    file.delete()
                } else {
                    throw IllegalArgumentException("Cached OCR image [$filePath] doesn't exist")
                }
            } catch (e: Exception) {
                Simber.e("OCR: Unable to delete cached scan file [$filePath]", e, tag = MULTI_FACTOR_ID)
                throw(e)
            }
        }
    }
}
