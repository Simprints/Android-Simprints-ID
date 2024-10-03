package com.simprints.infra.license

import com.simprints.infra.license.models.License
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


private fun License.isExpired(): Boolean {
    if (expiration.isNullOrEmpty()) return false

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    val expiryDate = try {
        format.parse(expiration)
    } catch (e: ParseException) {
        null
    }
    // if expiry date is null, consider it as not expired to avoid blocking the app and let the bio sdk handle it
    // if expiry date is before current date, consider it as expired
    return expiryDate?.before(Date()) ?: false
}

fun License?.determineLicenseStatus() = when {
    this == null -> LicenseStatus.MISSING
    isExpired() -> LicenseStatus.EXPIRED
    data.isEmpty() -> LicenseStatus.INVALID
    else -> LicenseStatus.VALID
}
