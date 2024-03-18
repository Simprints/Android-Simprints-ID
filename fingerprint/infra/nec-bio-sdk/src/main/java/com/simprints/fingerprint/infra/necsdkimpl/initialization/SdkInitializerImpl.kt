package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.Vendor
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.tools.toByteBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class SdkInitializerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val necInstance: NEC,
    private val licenseRepository: LicenseRepository,
) : SdkInitializer<Unit> {
    override suspend fun initialize(initializationParams: Unit?) {
        val licence = licenseRepository.getCachedLicense(Vendor.NEC)
        try {
            necInstance.init(licence.encodeAndConvertToByteBuffer(), context)
        } catch (e: Exception) {
            // if we fail to init NEC we should delete the license from the local storage
            // because it is most likely corrupted or expired license
            licenseRepository.deleteCachedLicense(Vendor.NEC)
            throw BioSdkException.BioSdkInitializationException(e)
        }
    }
}


private fun String.encodeAndConvertToByteBuffer() =
    EncodingUtilsImpl.base64ToBytes(this).toByteBuffer()


