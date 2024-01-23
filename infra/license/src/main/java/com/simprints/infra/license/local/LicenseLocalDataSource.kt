package com.simprints.infra.license.local

import com.simprints.infra.license.Vendor

internal interface LicenseLocalDataSource {
    suspend fun getLicense(vendor: Vendor): String?

    suspend fun saveLicense(vendor: Vendor, license: String)

    suspend fun deleteCachedLicense(vendor: Vendor)

    companion object {
        const val LICENSES_FOLDER = "licenses"
    }
}
