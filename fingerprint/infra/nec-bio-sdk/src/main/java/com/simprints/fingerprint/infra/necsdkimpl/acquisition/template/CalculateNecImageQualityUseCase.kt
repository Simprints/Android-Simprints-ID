package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.core.DispatcherBG
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class CalculateNecImageQualityUseCase @Inject constructor(
    private val necInstant: NEC,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(image: FingerprintImage): Int = withContext(dispatcher) {
        try {
            necInstant.qualityCheck(
                NecImage(
                    width = image.width,
                    height = image.height,
                    resolution = image.resolution,
                    imageBytes = image.imageBytes,
                ),
            )
        } catch (e: Exception) {
            throw BioSdkException.ImageQualityCheckingException(e)
        }
    }
}
