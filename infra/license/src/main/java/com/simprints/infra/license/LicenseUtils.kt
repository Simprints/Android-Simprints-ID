package com.simprints.infra.license

import com.simprints.infra.license.remote.License
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


private fun License.isExpired(): Boolean {
    if (expiration.isEmpty()) return false

    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    val expiryDate = format.parse(expiration)

    return expiryDate?.before(Date()) ?: false
}

fun License?.determineLicenseStatus() = when {
    this == null -> LicenseStatus.MISSING
    isExpired() -> LicenseStatus.EXPIRED
    data.isEmpty() -> LicenseStatus.INVALID
    else -> LicenseStatus.VALID
}
