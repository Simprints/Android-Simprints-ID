package com.simprints.infra.license

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.models.License
import com.simprints.infra.license.models.LicenseVersion
import org.junit.Test

class LicenseUtilsTest {
    @Test
    fun testDetermineLicenseStatus() {
        val nullLicense: License? = null
        assertThat(nullLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.MISSING)

        val expiredLicense = License(expiration = "2022-01-01T00:00:00Z", data = "data", version = LicenseVersion.UNLIMITED)
        assertThat(expiredLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.EXPIRED)

        val invalidLicense = License(expiration = "2223-01-01T00:00:00Z", data = "", version = LicenseVersion.UNLIMITED)
        assertThat(invalidLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.INVALID)

        val validLicense = License(expiration = "2123-01-01T00:00:00Z", data = "data", version = LicenseVersion.UNLIMITED)
        assertThat(validLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.VALID)

        val validLicenseWithEmptyExpiration = License(expiration = "", data = "data", version = LicenseVersion.UNLIMITED)
        assertThat(validLicenseWithEmptyExpiration.determineLicenseStatus()).isEqualTo(LicenseStatus.VALID)

        val validLicenseWithNullExpiration = License(expiration = null, data = "data", version = LicenseVersion.UNLIMITED)
        assertThat(validLicenseWithNullExpiration.determineLicenseStatus()).isEqualTo(LicenseStatus.VALID)

        val validLicenseWithBadExpirationDate = License(expiration = "bad date", data = "data", version = LicenseVersion.UNLIMITED)
        assertThat(validLicenseWithBadExpirationDate.determineLicenseStatus()).isEqualTo(
            LicenseStatus.VALID,
        )
    }
}
