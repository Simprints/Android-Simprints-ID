package com.simprints.infra.images.usecase

import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.images.model.Path
import javax.inject.Inject

internal class CreateSamplePathUseCase @Inject constructor() {
    operator fun invoke(
        sessionId: String,
        modality: GeneralConfiguration.Modality,
        sampleId: String,
        fileExtension: String,
    ) = Path(
        arrayOf(
            SESSIONS_PATH,
            sessionId,
            if (modality == GeneralConfiguration.Modality.FACE) FACES_PATH else FINGERPRINTS_PATH,
            "$sampleId.$fileExtension",
        ),
    )

    companion object {
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
