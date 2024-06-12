package com.simprints.fingerprint.infra.necsdkimpl.acquisition.template

import com.simprints.core.DispatcherIO
import com.simprints.fingerprint.infra.basebiosdk.acquisition.FingerprintTemplateProvider
import com.simprints.fingerprint.infra.basebiosdk.acquisition.domain.TemplateResponse
import com.simprints.fingerprint.infra.necsdkimpl.acquisition.image.ProcessedImageCache
import com.simprints.fingerprint.infra.scanner.capture.FingerprintCaptureWrapperFactory
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

internal class FingerprintTemplateProviderImpl @Inject constructor(

) :
    FingerprintTemplateProvider<FingerprintTemplateAcquisitionSettings, FingerprintTemplateMetadata> {


    override suspend fun acquireFingerprintTemplate(settings: FingerprintTemplateAcquisitionSettings?) =
        TemplateResponse(
            byteArrayOf(),
            FingerprintTemplateMetadata(
                templateFormat = NEC_TEMPLATE_FORMAT,
                imageQualityScore = 0
            )
        )

}

const val NEC_TEMPLATE_FORMAT = "NEC_1"
