package com.simprints.infralicense.local

internal interface LicenseLocalDataSource {
    fun getLicense(): String?

    fun saveLicense(license: String)

    fun deleteCachedLicense()

    companion object {
        const val LICENSES_FOLDER = "licenses"
        const val LICENSE_NAME = "ROC.lic"
    }
}
