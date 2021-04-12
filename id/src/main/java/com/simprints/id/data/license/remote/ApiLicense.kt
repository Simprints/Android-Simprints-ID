package com.simprints.id.data.license.remote

import com.fasterxml.jackson.annotation.JsonProperty
import com.simprints.id.data.license.repository.LicenseVendor

/**
 * ApiLicense only populates some fields, based on which vendor was asked when retrieving the license.
 */
data class ApiLicense(@JsonProperty("RANK_ONE_FACE") val rankOneLicense: RankOneLicense?) {

    /**
     * This method gets the correct license data based on which vendor is passed to it.
     * If the license doesn't contain data for that vendor, returns an empty string.
     */
    fun getLicenseBasedOnVendor(licenseVendor: LicenseVendor): String =
        when (licenseVendor) {
            LicenseVendor.RANK_ONE_FACE -> rankOneLicense?.data ?: ""
        }
}

data class RankOneLicense(val vendor: String, val expiration: String, val data: String)

/**
 * BFSID returns an error in the following format:
 * ```
 * { "error": "001" }
 * ```
 */
data class ApiLicenseError(val error: String)
