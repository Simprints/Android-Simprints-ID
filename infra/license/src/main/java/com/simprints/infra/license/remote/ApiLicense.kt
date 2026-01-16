package com.simprints.infra.license.remote

import androidx.annotation.Keep
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.license.models.Vendor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * ApiLicense only populates some fields, based on which vendor was asked when retrieving the license.
 */
@Keep
@Serializable
internal data class ApiLicense(
    val licenses: Map<Vendor, LicenseValue> = emptyMap(),
) {
    fun getLicenseBasedOnVendor(vendor: Vendor) = licenses[vendor]
}

@Keep
@Serializable
internal data class LicenseValue(
    val expiration: String? = null,
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
@Serializable
internal data class ApiLicenseError(
    val error: String,
)

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
internal fun String.parseApiLicense(apiJson: Json = JsonHelper.json): ApiLicense {
    val jsonObject = apiJson.decodeFromString<JsonObject>(this)

    val licenses = jsonObject.entries.associate { (key, value) ->
        Vendor.fromKey(key) to apiJson.decodeFromJsonElement<LicenseValue>(value)
    }

    return ApiLicense(licenses = licenses)
}
