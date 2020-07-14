package com.simprints.face.license.data.local

interface LicenseLocalDataSource {
    fun getLicense(): String?
    fun saveLicense(license: String)

    companion object {
        const val LICENSES_FOLDER = "licenses"
        const val LICENSE_NAME = "ROC.lic"
    }
}
