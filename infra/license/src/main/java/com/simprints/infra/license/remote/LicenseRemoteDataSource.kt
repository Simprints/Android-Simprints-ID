package com.simprints.infra.license.remote

import com.simprints.infra.license.models.LicenseVersion
import com.simprints.infra.license.models.Vendor

internal interface LicenseRemoteDataSource {
    suspend fun getLicense(
        projectId: String,
        deviceId: String,
        vendor: Vendor,
        version: LicenseVersion,
    ): ApiLicenseResult
}
