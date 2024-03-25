package com.simprints.infra.license

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.license.remote.License
import org.junit.Test

class LicenseUtilsTest {


    @Test
    fun testDetermineLicenseStatus() {
        val nullLicense: License? = null
        assertThat(nullLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.MISSING)

        val expiredLicense = License("2022-01-01T00:00:00Z", "data")
        assertThat(expiredLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.EXPIRED)

        val invalidLicense = License("2223-01-01T00:00:00Z", "")
        assertThat(invalidLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.INVALID)

        val validLicense = License("2123-01-01T00:00:00Z", "data")
        assertThat(validLicense.determineLicenseStatus()).isEqualTo(LicenseStatus.VALID)

        val validLicenseWithEmptyExpiration = License("", "data")
        assertThat(validLicenseWithEmptyExpiration.determineLicenseStatus()).isEqualTo(LicenseStatus.VALID)

        val validLicenseWithBadExpirationDate = License("bad date", "data")
        assertThat(validLicenseWithBadExpirationDate.determineLicenseStatus()).isEqualTo(
            LicenseStatus.VALID
        )

    }
}
