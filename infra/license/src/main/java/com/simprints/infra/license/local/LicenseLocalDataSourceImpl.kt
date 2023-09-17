package com.simprints.infra.license.local

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSES_FOLDER
import com.simprints.infra.logging.Simber
import com.simprints.infra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class LicenseLocalDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyHelper: SecurityManager,
    @DispatcherIO private val dispatcherIo: CoroutineDispatcher,
) : LicenseLocalDataSource {

    private val licensePath = "${context.filesDir}/${LICENSES_FOLDER}"

    init {
        createDirectoryIfNonExistent(licensePath)
    }


    override suspend fun getLicense(vendor: String): String? = withContext(dispatcherIo) {
        renameOldRocLicense()// TODO: remove this after a few releases when all users have migrated to the 2023.3.0 version
        getFileFromStorage(vendor)
    }
    private fun renameOldRocLicense() {
        // check if there is a ROC.lic file rename it to RANK_ONE_FACE to match the new license name
        val oldLicensePath = "$licensePath/ROC.lic"
        val oldLicenseFile = File(oldLicensePath)
        if (oldLicenseFile.exists()) {
            val newLicensePath = "$licensePath/RANK_ONE_FACE"
            oldLicenseFile.renameTo(File(newLicensePath))
        }

    }
    override suspend fun saveLicense(vendor: String, license: String): Unit =
        withContext(dispatcherIo) {
            createDirectoryIfNonExistent(licensePath)

            val file = File("$licensePath/$vendor")

            try {
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

    override suspend fun deleteCachedLicense(): Unit = withContext(dispatcherIo) {
        try {
            val deleted = File(licensePath).delete()
            Simber.d("Deleted cached licenses successfully = $deleted")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun getFileFromStorage(vendor: String): String? = try {
        val file = File("$licensePath/$vendor")
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, context)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

}
