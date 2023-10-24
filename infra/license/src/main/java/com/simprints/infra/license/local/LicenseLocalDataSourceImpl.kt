package com.simprints.infra.license.local

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.infra.license.Vendor
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

    private val licenseDirectoryPath = "${context.filesDir}/${LICENSES_FOLDER}"


    override suspend fun getLicense(vendor: Vendor): String? = withContext(dispatcherIo) {
        renameOldRocLicense()// TODO: remove this after a few releases when all users have migrated to the 2023.3.0 version
        getFileFromStorage(vendor)
    }

    private fun renameOldRocLicense() {
        // check if there is a ROC.lic file rename it to RANK_ONE_FACE to match the new license name
        val oldLicensePath = "$licenseDirectoryPath/ROC.lic"
        val oldLicenseFile = File(oldLicensePath)
        if (oldLicenseFile.exists()) {
            val newLicensePath = "$licenseDirectoryPath/RANK_ONE_FACE"
            if (!oldLicenseFile.renameTo(File(newLicensePath))) {
                throw Exception("Failed to rename old ROC.lic license file to RANK_ONE_FACE")
            }
        }

    }

    override suspend fun saveLicense(vendor: Vendor, license: String): Unit =
        withContext(dispatcherIo) {
            createDirectoryIfNonExistent(licenseDirectoryPath)

            val file = File("$licenseDirectoryPath/${vendor}")

            try {
                keyHelper.getEncryptedFileBuilder(file, context).openFileOutput()
                    .use { it.write(license.toByteArray()) }
            } catch (t: Throwable) {
                Simber.e(t)
            }
        }

    private fun createDirectoryIfNonExistent(path: String) {
        val directory = File(path)
        if (!directory.exists())
            directory.mkdirs()
    }

    override suspend fun deleteCachedLicense(): Unit = withContext(dispatcherIo) {
        try {
            val deleted = File(licenseDirectoryPath).delete()
            Simber.d("Deleted cached licenses successfully = $deleted")
        } catch (t: Throwable) {
            Simber.e(t)
        }
    }

    private fun getFileFromStorage(vendor: Vendor): String? = try {
        val file = File("$licenseDirectoryPath/$vendor")
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, context)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

}
