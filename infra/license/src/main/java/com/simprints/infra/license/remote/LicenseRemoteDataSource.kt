package com.simprints.infra.license.remote

import com.simprints.infra.license.Vendor


internal interface LicenseRemoteDataSource {
    suspend fun getLicense(projectId: String, deviceId: String, vendor: Vendor): ApiLicenseResult
}
