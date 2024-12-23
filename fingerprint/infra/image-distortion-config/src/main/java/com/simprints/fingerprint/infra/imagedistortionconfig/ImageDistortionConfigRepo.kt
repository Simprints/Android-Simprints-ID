package com.simprints.fingerprint.infra.imagedistortionconfig

import com.simprints.fingerprint.infra.imagedistortionconfig.local.ImageDistortionConfigLocalRepo
import com.simprints.fingerprint.infra.imagedistortionconfig.remote.ImageDistortionConfigRemoteRepo
import javax.inject.Inject

class ImageDistortionConfigRepo @Inject internal constructor(
    private val localRepo: ImageDistortionConfigLocalRepo,
    private val remoteRepo: ImageDistortionConfigRemoteRepo,
) {
    suspend fun saveConfig(
        scannerId: String,
        serialNumber: String,
        configFile: ByteArray,
    ) {
        localRepo.saveConfig(scannerId, serialNumber, configFile)
        uploadPendingConfigs()
    }

    suspend fun uploadPendingConfigs(): Boolean {
        val pendingConfigs = localRepo.getPendingUploads()
        for (config in pendingConfigs) {
            val isUploaded = remoteRepo.uploadConfig(
                config.serialNumber,
                config.configFile,
            )
            if (isUploaded) {
                localRepo.markAsUploaded(config.id)
            } else {
                return false
            }
        }
        return true
    }

    suspend fun getConfig(scannerId: String) = localRepo.getConfigFile(scannerId)
}
