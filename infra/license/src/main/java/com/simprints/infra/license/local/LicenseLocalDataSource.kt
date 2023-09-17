package com.simprints.infra.license.local

internal interface LicenseLocalDataSource {
    suspend fun getLicense(vendor: String): String?

    suspend fun saveLicense(vendor: String, license: String)

    suspend fun deleteCachedLicense()

    companion object {
        const val LICENSES_FOLDER = "licenses"
    }
}
