package com.simprints.infra.license.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSES_FOLDER
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSE_NAME
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import java.io.File
import javax.inject.Inject

internal class LicenseLocalDataSourceImpl @Inject constructor(
    private val context: Context,
    private val keyHelper: SecurityManager
) : LicenseLocalDataSource {

    private val licensePath = "${context.filesDir}/${LICENSES_FOLDER}/${LICENSE_NAME}"

    init {
        createDirectoryIfNonExistent(licensePath)
    }

    override fun getLicense(): String? = getFileFromStorage() ?: getFileFromAssets()

    override fun saveLicense(license: String) {
        createDirectoryIfNonExistent(licensePath)

        val file = File(licensePath)

        return try {
            keyHelper.getEncryptedFileBuilder(file, context).openFileOutput()
                .use { it.write(license.toByteArray()) }
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val file = File(path)
        val fileName = file.name
        val directory = File(path.replace(fileName, ""))

        if (!directory.exists())
            directory.mkdirs()
    }

    @VisibleForTesting
    fun getFileFromAssets(): String? = try {
        context.assets.open(LICENSE_NAME).use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

    override fun deleteCachedLicense() {
        try {
            File(licensePath).delete()
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun getFileFromStorage(): String? = try {
        val file = File(licensePath)
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, context)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

}
