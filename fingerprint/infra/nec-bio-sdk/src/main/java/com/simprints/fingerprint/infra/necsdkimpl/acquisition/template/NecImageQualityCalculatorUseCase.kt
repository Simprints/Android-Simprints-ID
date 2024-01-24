package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import javax.inject.Inject

class NecImageQualityCalculatorUseCase @Inject constructor(
    private val necInstant: NEC,
) {
    operator fun invoke(image: FingerprintImage): Int =
        try {
            necInstant.qualityCheck(
                NecImage(
                    width = image.width,
                    height = image.height,
                    resolution = image.resolution,
                    imageBytes = image.imageBytes
                )
            )
        } catch (e: Exception) {
            throw BioSdkException.ImageQualityCheckingException(e)
        }
}
