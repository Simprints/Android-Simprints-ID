package com.simprints.infra.images.usecase

import com.simprints.core.tools.utils.EncodingUtils
import java.io.IOException
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import javax.inject.Inject

/**
 * This operation consumes the input stream and since the encrypted file stream does
 * not support mark/reset, the file needs to be re-opened again on the upload.
 *
 * For this reason un-encrypted file size is also calculated during md5 calculation
 * to be used between stream reads.
 */
internal class CalculateFileMd5AndSizeUseCase @Inject constructor(
    private val encodingUtils: EncodingUtils,
) {
    operator fun invoke(inputStream: InputStream): CalculationResult = try {
        val md5 = MessageDigest.getInstance("MD5")
        var size = 0L

        DigestInputStream(inputStream, md5).use { dis ->
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead = 0
            while (dis.read(buffer).also { bytesRead = it } != -1) {
                // Appending directly in the check causes off-by-one error due to "-1" marker
                size += bytesRead
                // No explicit update to 'md' needed here, DigestInputStream handles it.
            }
        }

        val digest = md5.digest()

        CalculationResult(
            md5 = encodingUtils.byteArrayToBase64(digest),
            size = size,
        )
    } catch (e: IOException) {
        e.printStackTrace()
        CalculationResult("", 0)
    }

    companion object Companion {
        private const val BUFFER_SIZE = 8 * 1024 // 8K as suggested by AI
    }

    internal data class CalculationResult(
        val md5: String,
        val size: Long,
    )
}
