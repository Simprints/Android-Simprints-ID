package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseStatus
import com.simprints.infra.license.SaveLicenseCheckEventUseCase
import com.simprints.infra.license.Vendor
import com.simprints.infra.license.determineLicenseStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SdkInitializerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val licenseRepository: LicenseRepository,
    private val saveLicenseCheck: SaveLicenseCheckEventUseCase
) : SdkInitializer<Unit> {
    override suspend fun initialize(initializationParams: Unit?) {
        var licenseStatus: LicenseStatus? = null
        try {
            val licence = licenseRepository.getCachedLicense(Vendor.NEC)
            licenseStatus = licence.determineLicenseStatus()
            if (licenseStatus != LicenseStatus.VALID) {
                throw BioSdkException.BioSdkInitializationException(message = "License is $licenseStatus")
            }
        } catch (e: Exception) {
            licenseRepository.deleteCachedLicense(Vendor.NEC)
            licenseStatus = LicenseStatus.ERROR
            throw BioSdkException.BioSdkInitializationException(e)
        } finally {
            licenseStatus?.let { saveLicenseCheck(Vendor.NEC, it) }
        }
    }
}

