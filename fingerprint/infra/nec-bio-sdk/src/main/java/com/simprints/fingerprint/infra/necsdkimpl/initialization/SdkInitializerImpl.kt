package com.simprints.fingerprint.infra.necsdkimpl.initialization

import android.content.Context
import com.simprints.core.DeviceID
import com.simprints.core.tools.utils.EncodingUtilsImpl
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.fingerprint.infra.basebiosdk.initialization.SdkInitializer
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.template.log
import com.simprints.infra.authstore.AuthStore
import com.simprints.infra.license.LicenseRepository
import com.simprints.infra.license.LicenseState
import com.simprints.infra.license.Vendor
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.tools.toByteBuffer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.nio.ByteBuffer
import javax.inject.Inject

class SdkInitializerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @DeviceID private val deviceId: String,
    private val necInstance: NEC,
    private val licenseRepository: LicenseRepository,
    private val authStore: AuthStore
) : SdkInitializer<Unit> {
    override suspend fun initialize(initializationParams: Unit?) =
        // download or get license from local storage
        // block until license is retrieved or an error occurs
        licenseRepository.getLicenseStates(
            authStore.signedInProjectId, deviceId, NEC_VENDOR
        ).first {
            //ignore all states except finished states
            !(it is LicenseState.Downloading || it is LicenseState.Started)
        }
            .let {
                when (it) {
                    is LicenseState.FinishedWithSuccess -> {
                        log("License downloaded successfully")
                        initNecSdk(it.license.encodeAndConvertToByteBuffer(), context)
                    }

                    is LicenseState.FinishedWithError -> {
                        log("Error downloading license ${it.errorCode}")
                        throw BioSdkException.LicenseDownloadException()
                    }

                    is LicenseState.FinishedWithBackendMaintenanceError -> {
                        throw BioSdkException.LicenseDownloadMaintenanceModeException(it.estimatedOutage?.toString())
                    }

                    else -> {
                        // unreachable code as we are filtering out all other states
                    }
                }
            }


    private suspend fun initNecSdk(licence: ByteBuffer, context: Context) {
        try {
            necInstance.init(licence, context)
        } catch (e: Exception) {
            log("Error initializing NEC SDK ${e.message}")
            // if we fail to initialize the SDK we should delete the license from the local storage
            // because it is most likely corrupted
            licenseRepository.deleteCachedLicense()
            throw BioSdkException.BioSdkInitializationException(e)
        }
    }

    companion object {
        val NEC_VENDOR = Vendor("NEC_FINGERPRINT")
    }
}


private fun String.encodeAndConvertToByteBuffer() =
    EncodingUtilsImpl.base64ToBytes(this).toByteBuffer()


