package com.simprints.id.data.db.event.domain.models.subject

import androidx.annotation.Keep
import com.simprints.core.tools.EncodingUtils
import com.simprints.id.data.db.event.domain.models.Event
import com.simprints.id.data.db.event.domain.models.EventLabels
import com.simprints.id.data.db.event.domain.models.EventPayload
import com.simprints.id.data.db.event.domain.models.EventType
import com.simprints.id.data.db.event.domain.models.EventType.ENROLMENT_RECORD_CREATION
import com.simprints.id.data.db.subject.domain.FaceSample
import com.simprints.id.data.db.subject.domain.FingerprintSample
import com.simprints.id.data.db.subject.domain.uniqueId
import com.simprints.id.domain.modality.Modes
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
        extraLabels.copy(subjectId = subjectId, projectId = projectId, moduleIds = listOf(moduleId), attendantId = attendantId, mode = modes),
        EnrolmentRecordCreationPayload(createdAt, EVENT_VERSION, subjectId, projectId, moduleId, attendantId, biometricReferences),
        ENROLMENT_RECORD_CREATION)

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

        fun buildBiometricReferences(fingerprintSamples: List<FingerprintSample>,
                                     faceSamples: List<FaceSample>): List<BiometricReference> {
            val biometricReferences = mutableListOf<BiometricReference>()

            buildFingerprintReference(fingerprintSamples)?.let {
                biometricReferences.add(it)
            }

            buildFaceReference(faceSamples)?.let {
                biometricReferences.add(it)
            }

            return biometricReferences
        }

        private fun buildFingerprintReference(fingerprintSamples: List<FingerprintSample>) =
            if (fingerprintSamples.isNotEmpty()) {
                FingerprintReference(
                    fingerprintSamples.uniqueId(),
                    fingerprintSamples.map {
                        FingerprintTemplate(
                            it.templateQualityScore,
                            EncodingUtils.byteArrayToBase64(it.template),
                            it.fingerIdentifier.fromSubjectToEvent())
                    }
                )
            } else {
                null
            }

        private fun buildFaceReference(faceSamples: List<FaceSample>) =
            if (faceSamples.isNotEmpty()) {
                FaceReference(
                    faceSamples.uniqueId(),
                    faceSamples.map {
                        FaceTemplate(
                            EncodingUtils.byteArrayToBase64(it.template)
                        )
                    }
                )
            } else {
                null
            }

        const val EVENT_VERSION = 2
    }
}
