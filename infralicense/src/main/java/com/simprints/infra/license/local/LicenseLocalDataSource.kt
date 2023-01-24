package com.simprints.infra.license.local

internal interface LicenseLocalDataSource {
    suspend fun getLicense(): String?

    suspend fun saveLicense(license: String)

    suspend fun deleteCachedLicense()

    companion object {
        const val LICENSES_FOLDER = "licenses"
        const val LICENSE_NAME = "ROC.lic"
    }
}
