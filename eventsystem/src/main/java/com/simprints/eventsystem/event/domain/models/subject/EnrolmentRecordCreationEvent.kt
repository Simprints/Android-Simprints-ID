package com.simprints.eventsystem.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.domain.face.FaceSample
import com.simprints.core.domain.face.uniqueId
import com.simprints.core.domain.fingerprint.FingerprintSample
import com.simprints.core.domain.fingerprint.uniqueId
import com.simprints.core.domain.modality.Modes
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.eventsystem.event.domain.models.Event
import com.simprints.eventsystem.event.domain.models.EventLabels
import com.simprints.eventsystem.event.domain.models.EventPayload
import com.simprints.eventsystem.event.domain.models.EventType
import com.simprints.eventsystem.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.eventsystem.event.domain.models.face.fromModuleApiToDomain
import com.simprints.eventsystem.event.domain.models.fingerprint.fromModuleApiToDomain
import java.util.*

@Keep
data class EnrolmentRecordCreationEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: EnrolmentRecordCreationPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        subjectId: String,
        projectId: String,
        moduleId: String,
        attendantId: String,
        modes: List<Modes>,
        biometricReferences: List<BiometricReference>,
        extraLabels: EventLabels = EventLabels()
    ) : this(
        UUID.randomUUID().toString(),
        extraLabels.copy(
            subjectId = subjectId,
            projectId = projectId,
            moduleIds = listOf(moduleId),
            attendantId = attendantId,
            mode = modes
        ),
        EnrolmentRecordCreationPayload(
            createdAt,
            EVENT_VERSION,
            subjectId,
            projectId,
            moduleId,
            attendantId,
            biometricReferences
        ),
        ENROLMENT_RECORD_CREATION
    )

    data class EnrolmentRecordCreationPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val subjectId: String,
        val projectId: String,
        val moduleId: String,
        val attendantId: String,
        val biometricReferences: List<BiometricReference>,
        override val type: EventType = ENROLMENT_RECORD_CREATION,
        override val endedAt: Long = 0
    ) : EventPayload()

    companion object {

        fun buildBiometricReferences(
            fingerprintSamples: List<FingerprintSample>,
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils
        ): List<BiometricReference> {
            val biometricReferences = mutableListOf<BiometricReference>()

            buildFingerprintReference(fingerprintSamples, encoder)?.let {
                biometricReferences.add(it)
            }

            buildFaceReference(faceSamples, encoder)?.let {
                biometricReferences.add(it)
            }

            return biometricReferences
        }

        private fun buildFingerprintReference(
            fingerprintSamples: List<FingerprintSample>,
            encoder: EncodingUtils
        ) =
            if (fingerprintSamples.isNotEmpty()) {
                FingerprintReference(
                    fingerprintSamples.uniqueId() ?: "",
                    fingerprintSamples.map {
                        FingerprintTemplate(
                            it.templateQualityScore,
                            encoder.byteArrayToBase64(it.template),
                            it.fingerIdentifier
                        )
                    },
                    fingerprintSamples.first().format.fromModuleApiToDomain()
                )
            } else {
                null
            }

        private fun buildFaceReference(
            faceSamples: List<FaceSample>,
            encoder: EncodingUtils
        ) =
            if (faceSamples.isNotEmpty()) {
                FaceReference(
                    faceSamples.uniqueId() ?: "",
                    faceSamples.map {
                        FaceTemplate(
                            encoder.byteArrayToBase64(it.template)
                        )
                    },
                    faceSamples.first().format.fromModuleApiToDomain()
                )
            } else {
                null
            }

        const val EVENT_VERSION = 3
    }
}
