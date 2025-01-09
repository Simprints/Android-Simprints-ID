package com.simprints.fingerprint.infra.imagedistortionconfig.local

import javax.inject.Inject

internal class ImageDistortionConfigLocalRepo @Inject constructor(
    private val dao: ImageDistortionConfigDao,
) {
    suspend fun saveConfig(
        scannerId: String,
        un20SerialNumber: String,
        configFile: ByteArray,
    ) {
        val config = DbImageDistortionConfig(
            scannerId = scannerId,
            un20SerialNumber = un20SerialNumber,
            configFile = configFile,
            isUploaded = false,
        )
        dao.insertConfig(config)
    }

    suspend fun getPendingUploads(): List<DbImageDistortionConfig> = dao.getPendingUploads()

    suspend fun markAsUploaded(configId: Int) {
        dao.markAsUploaded(configId)
    }

    suspend fun getConfigFile(scannerId: String) = dao.getConfigByScannerId(scannerId)?.configFile
}
