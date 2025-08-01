package com.simprints.infra.images.usecase

import com.simprints.infra.config.store.models.GeneralConfiguration
import com.simprints.infra.images.model.Path
import javax.inject.Inject

internal class SamplePathConverter @Inject constructor() {
    fun create(
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

    fun extract(path: Path): PathData? {
        // path structure is: projects/{projectId}/sessions/{sessionId}/{faces|fingerprints}/{sampleId}.{extension}
        val parts = path.parts.takeIf { it.size > 3 } ?: return null
        val sessionsPathIndex = parts.indexOf(SESSIONS_PATH).takeIf { it >= 0 } ?: return null
        val sessionId = parts[sessionsPathIndex + 1]
        val modality = parts[sessionsPathIndex + 2].let {
            when (it) {
                FACES_PATH -> GeneralConfiguration.Modality.FACE
                else -> GeneralConfiguration.Modality.FINGERPRINT
            }
        }
        val sampleId = parts[sessionsPathIndex + 3].substringBefore(".")

        return PathData(sessionId, modality, sampleId)
    }

    data class PathData(
        val sessionId: String,
        val modality: GeneralConfiguration.Modality,
        val sampleId: String,
    )

    companion object Companion {
        const val SESSIONS_PATH = "sessions"
        const val FACES_PATH = "faces"
        const val FINGERPRINTS_PATH = "fingerprints"
    }
}
