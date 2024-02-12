package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.basebiosdk.exceptions.BioSdkException
import com.simprints.necwrapper.nec.NEC
import com.simprints.necwrapper.nec.models.NecImage
import javax.inject.Inject

class ExtractNecTemplateUseCase @Inject constructor(private val nec: NEC) {
    operator fun invoke(
        fingerprintImage: FingerprintImage,
        qualityScore: Int
    ): TemplateResponse<FingerprintTemplateMetadata> {
        try {
            val template = nec.extract(
                NecImage(
                    width = fingerprintImage.width,
                    height = fingerprintImage.height,
                    resolution = fingerprintImage.resolution,
                    imageBytes = fingerprintImage.imageBytes
                )
            )
            return TemplateResponse(
                template.bytes,
                FingerprintTemplateMetadata(
                    templateFormat = NEC_TEMPLATE_FORMAT,
                    imageQualityScore = qualityScore
                )
            )
        } catch (e: Exception) {
            throw BioSdkException.TemplateExtractionException(e)
        }
    }
}
