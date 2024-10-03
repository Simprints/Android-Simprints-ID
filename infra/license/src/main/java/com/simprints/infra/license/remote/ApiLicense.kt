package com.simprints.infra.license.remote

import androidx.annotation.Keep
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.license.models.Vendor

/**
 * ApiLicense only populates some fields, based on which vendor was asked when retrieving the license.
 */
@Keep
internal data class ApiLicense(val licenses: Map<Vendor, LicenseValue> = emptyMap()) {
    fun getLicenseBasedOnVendor(vendor: Vendor) = licenses[vendor]
}

@Keep
internal data class LicenseValue(
    val expiration: String?,
    val data: String,
    val version: String,
)

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
            Vendor.fromKey(entry.key) to JsonHelper.jackson.treeToValue(entry.value, LicenseValue::class.java)
        }.toMap()
    )
}

