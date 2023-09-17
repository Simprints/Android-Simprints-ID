package com.simprints.infra.license.remote

import androidx.annotation.Keep
import com.simprints.core.tools.json.JsonHelper

/**
 * ApiLicense only populates some fields, based on which vendor was asked when retrieving the license.
 */
@Keep
internal data class ApiLicense(val licenses: Map<String, License> = emptyMap()) {

    /**
     * This method gets the correct license data based on which vendor is passed to it.
     * If the license doesn't contain data for that vendor, returns an empty string.
     */
    fun getLicenseBasedOnVendor(vendor: String) = licenses[vendor]?.data ?: ""

}

@Keep
internal data class License(val vendor: String, val expiration: String, val data: String)

/**
 * BFSID returns an error in the following format:
 * ```
 * { "error": "001" }
 * ```
 */
@Keep
internal data class ApiLicenseError(val error: String)


/**
 * Parse api license

 * example json:
 *
 * ```
 * {
 *  "RANK_ONE_FACE": {
 *    "vendor": "RANK_ONE_FACE",
 *    "expiration": "2023-12-31",
 *    data: "..."
 *    }
 *    "NEC_FINGERPRINT": {
 *    "vendor": "NEC_FINGERPRINT",
 *    "expiration": "2023-12-31",
 *    data: "..."
 *    }
 * }
 *    ```
 * @return ApiLicense
 */
internal fun String.parseApiLicense(): ApiLicense = JsonHelper.jackson.readTree(this).let {
    return ApiLicense(
        licenses = it.fields().asSequence().map { entry ->
            entry.key to JsonHelper.jackson.treeToValue(entry.value, License::class.java)
        }.toMap()
    )
}

