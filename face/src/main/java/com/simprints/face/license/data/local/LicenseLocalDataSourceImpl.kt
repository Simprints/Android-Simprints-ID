package com.simprints.face.license.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import com.simprints.face.license.data.local.LicenseLocalDataSource.Companion.LICENSES_FOLDER
import com.simprints.face.license.data.local.LicenseLocalDataSource.Companion.LICENSE_NAME
import timber.log.Timber
import java.io.File

class LicenseLocalDataSourceImpl(val context: Context) : LicenseLocalDataSource {
    private val licensePath = "${context.filesDir}/${LICENSES_FOLDER}/${LICENSE_NAME}"
    private val masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC)

    init {
        createDirectoryIfNonExistent(licensePath)
    }

    override fun getLicense(): String? = getFileFromStorage() ?: getFileFromAssets()

    override fun saveLicense(license: String) {
        createDirectoryIfNonExistent(licensePath)

        val file = File(licensePath)

        return try {
            getEncryptedFile(file).openFileOutput().use { it.write(license.toByteArray()) }
        } catch (t: Throwable) {
            Timber.e(t)
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
            Timber.e(t)
        }
    }

    private fun getFileFromStorage(): String? = try {
        val file = File(licensePath)
        val encryptedFile = getEncryptedFile(file)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

    private fun getEncryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

}
