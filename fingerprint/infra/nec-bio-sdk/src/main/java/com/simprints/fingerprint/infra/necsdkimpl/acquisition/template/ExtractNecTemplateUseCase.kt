package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.core.DispatcherBG
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ExtractNecTemplateUseCase @Inject constructor(
    private val nec: NEC,
    @param:DispatcherBG private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        fingerprintImage: FingerprintImage,
        qualityScore: Int,
    ): TemplateResponse<FingerprintTemplateMetadata> = withContext(dispatcher) {
        try {
            val template = nec.extract(
                NecImage(
                    width = fingerprintImage.width,
                    height = fingerprintImage.height,
                    resolution = fingerprintImage.resolution,
                    imageBytes = fingerprintImage.imageBytes,
                ),
            )
            TemplateResponse(
                template.bytes,
                FingerprintTemplateMetadata(
                    templateFormat = NEC_TEMPLATE_FORMAT,
                    imageQualityScore = qualityScore,
                ),
            )
        } catch (e: Exception) {
            throw BioSdkException.TemplateExtractionException(e)
        }
    }
}
