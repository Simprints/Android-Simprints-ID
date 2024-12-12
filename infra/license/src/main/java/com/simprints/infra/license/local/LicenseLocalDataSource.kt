package com.simprints.infra.license.local

import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.Vendor

internal interface LicenseLocalDataSource {
    suspend fun getLicense(vendor: Vendor): License?

    suspend fun saveLicense(
        vendor: Vendor,
        license: License,
    )

    suspend fun deleteCachedLicense(vendor: Vendor)

    suspend fun deleteCachedLicenses()

    companion object {
        const val LICENSES_FOLDER = "licenses"
    }
}
