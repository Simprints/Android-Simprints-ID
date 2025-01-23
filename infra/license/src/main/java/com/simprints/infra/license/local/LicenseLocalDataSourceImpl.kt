package com.simprints.infra.license.local

import android.content.Context
import com.simprints.core.DispatcherIO
import com.simprints.infra.license.local.LicenseLocalDataSource.Companion.LICENSES_FOLDER
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.LICENSE
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

    override suspend fun getLicense(vendor: Vendor): License? = withContext(dispatcherIo) {
        renameOldRocLicense() // TODO: remove this after a few releases when all users have migrated to the 2023.3.0 version

        val version = getHighestAvailableVersion(vendor)
        val expirationDate = getExpirationDate(vendor, version)
        val licenseData = getFileFromStorage(vendor, version)

        licenseData?.let { License(expirationDate, it, version) }
    }

    private fun getHighestAvailableVersion(vendor: Vendor): LicenseVersion = File("$licenseDirectoryPath/${vendor.value}")
        .takeIf { it.isDirectory } // If not directory, then there will only be a single version
        ?.listFiles()
        ?.map { it.name }
        ?.sortedWith(vendor.versionComparator)
        ?.last()
        .let { LicenseVersion(it.orEmpty()) }

    private fun getExpirationDate(
        vendor: Vendor,
        version: LicenseVersion,
    ): String {
        // if the vendor.expiration file exists, read the expiration date from it else return an empty string
        // expiration date is stored in a file with the vendor name and .expiration extension
        // no need to encrypt the expiration date as it is not sensitive information
        val expirationFile = getExpirationFile(vendor, version)
        return if (expirationFile.exists()) {
            expirationFile.readText()
        } else {
            ""
        }
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

    override suspend fun saveLicense(
        vendor: Vendor,
        license: License,
    ): Unit = withContext(dispatcherIo) {
        createDirectoryIfNonExistent(licenseDirectoryPath)
        saveLicenseData(vendor, license.version, license.data)
        license.expiration?.let { saveExpirationDate(vendor, license.version, it) }
    }

    private fun saveLicenseData(
        vendor: Vendor,
        version: LicenseVersion,
        licenseData: String,
    ) {
        val file = getLicenceFile(vendor, version)
        try {
            file.parentFile?.mkdirs()
            keyHelper
                .getEncryptedFileBuilder(file, context)
                .openFileOutput()
                .use { it.write(licenseData.toByteArray()) }
        } catch (t: Throwable) {
            Simber.e("Failed to save licence data for ${vendor.value}", t, tag = LICENSE)
        }
    }

    private fun saveExpirationDate(
        vendor: Vendor,
        version: LicenseVersion,
        expirationDate: String,
    ) {
        try {
            getExpirationFile(vendor, version).writeText(expirationDate)
        } catch (t: Throwable) {
            Simber.e("Failed to save licence expiration date for ${vendor.value}", t, tag = LICENSE)
        }
    }

    private fun createDirectoryIfNonExistent(path: String) {
        val directory = File(path)
        if (!directory.exists()) {
            directory.mkdirs()
        }
    }

    override suspend fun deleteCachedLicense(vendor: Vendor): Unit = withContext(dispatcherIo) {
        try {
            val deleted = File("$licenseDirectoryPath/${vendor.value}").deleteRecursively()
            Simber.d("Deleted cached licenses successfully = $deleted", tag = LICENSE)
        } catch (t: Throwable) {
            Simber.e("Failed to delete cached licenses for ${vendor.value}", t, tag = LICENSE)
        }
    }

    override suspend fun deleteCachedLicenses(): Unit = withContext(dispatcherIo) {
        try {
            val deleted = File(licenseDirectoryPath).deleteRecursively()
            Simber.d("Deleted all licenses successfully = $deleted", tag = LICENSE)
        } catch (t: Throwable) {
            Simber.e("Failed to delete licenses", t, tag = LICENSE)
        }
    }

    private fun getFileFromStorage(
        vendor: Vendor,
        version: LicenseVersion,
    ): String? = try {
        val file = getLicenceFile(vendor, version)
        val encryptedFile = keyHelper.getEncryptedFileBuilder(file, context)
        encryptedFile.openFileInput().use { String(it.readBytes()) }
    } catch (t: Throwable) {
        null
    }

    private fun getLicenceFile(
        vendor: Vendor,
        version: LicenseVersion,
    ) = File(
        if (version.isLimited) {
            "$licenseDirectoryPath/${vendor.value}/${version.value}"
        } else {
            "$licenseDirectoryPath/${vendor.value}"
        },
    )

    private fun getExpirationFile(
        vendor: Vendor,
        version: LicenseVersion,
    ) = File(
        if (version.isLimited) {
            "$licenseDirectoryPath/${vendor.value}_${version.value}_expiration"
        } else {
            "$licenseDirectoryPath/${vendor.value}_expiration"
        },
    )
}
