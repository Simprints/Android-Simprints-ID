package com.simprints.infra.credential.store

import android.content.Context
import android.graphics.Bitmap
import com.simprints.core.DispatcherIO
import com.simprints.infra.credential.store.model.CredentialScanImageType
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.MULTI_FACTOR_ID
import com.simprints.infra.logging.Simber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CredentialImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Saves a bitmap to the application's cache directory as a JPEG file.
     * @param bitmap the bitmap to save
     * @param credential associated external credential data
     * @param imageType type of an image: [CredentialScanImageType.FullDocument] if image contains entire document or
     * [CredentialScanImageType.ZoomedInCredential] if image contains zoomed-in part of the external credential
     *
     * @return absolute path to the saved file
     */
    suspend fun saveCredentialScan(
        bitmap: Bitmap,
        imageType: CredentialScanImageType,
    ): String = withContext(ioDispatcher) {
        val file = imageType.toFile()
        file.outputStream().use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        return@withContext file.absolutePath
    }

    /**
     * Deletes cached scan file
     */
    suspend fun deleteByPath(path: String) = with(ioDispatcher) {
        try {
            val file = File(path)
            if (file.exists()) {
                if (file.delete()) {
                    Simber.d("Deleted credential scan: ${file.absolutePath}", tag = MULTI_FACTOR_ID)
                } else {
                    Simber.d("Failed to delete credential scan: ${file.absolutePath}", tag = MULTI_FACTOR_ID)
                }
            }
        } catch (e: Exception) {
            Simber.e("Unable to delete cached scan file [$path]", e, tag = MULTI_FACTOR_ID)
            throw (e)
        }
    }

    /**
     * Deletes all cached scans associated with the give external credential
     */
    suspend fun deleteAllCredentialScans() = CredentialScanImageType.entries.forEach { imageType ->
        deleteByPath(imageType.toFile().absolutePath)
    }

    private fun CredentialScanImageType.toFile(): File = File(context.cacheDir, toFileName())

    private fun CredentialScanImageType.toFileName(): String = "credential_${this.toString().normalize()}_${System.currentTimeMillis()}.jpg"

    private fun String.normalize() = trim().replace(" ", "")
}
