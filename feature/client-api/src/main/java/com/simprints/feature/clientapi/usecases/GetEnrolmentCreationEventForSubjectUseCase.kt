package com.simprints.feature.clientapi.usecases

import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.store.models.canCoSyncAllData
import com.simprints.infra.config.store.models.canCoSyncBiometricData
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordEvents
import com.simprints.infra.events.event.cosync.v1.TokenizableStringV1
import com.simprints.infra.events.event.cosync.v1.TokenizableStringV1Deserializer
import com.simprints.infra.events.event.cosync.v1.TokenizableStringV1Serializer
import com.simprints.infra.events.event.cosync.v1.toCoSyncV1
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
import com.simprints.infra.logging.Simber
import javax.inject.Inject

internal class GetEnrolmentCreationEventForSubjectUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val enrolmentRecordRepository: EnrolmentRecordRepository,
    private val encoder: EncodingUtils,
    private val jsonHelper: JsonHelper,
) {
    suspend operator fun invoke(
        projectId: String,
        subjectId: String,
    ): String? {
        val config = configManager.getProjectConfiguration()

        if (!config.canCoSyncAllData() && !config.canCoSyncBiometricData()) {
            return null
        }

        val recordCreationEvent = enrolmentRecordRepository
            .load(SubjectQuery(projectId = projectId, subjectId = subjectId))
            .firstOrNull()
            ?.fromSubjectToEnrolmentCreationEvent()

        if (recordCreationEvent == null) {
            Simber.e(
                "Couldn't find enrolment for subjectActions",
                IllegalStateException("No enrolment record found for subjectId: $subjectId"),
            )
            return null
        }

        // Convert to V1 external schema before serialization for stable contract
        val v1Events = EnrolmentRecordEvents(listOf(recordCreationEvent)).toCoSyncV1()
        return jsonHelper.toJson(v1Events, coSyncSerializationModule)
    }

    private fun Subject.fromSubjectToEnrolmentCreationEvent() = EnrolmentRecordCreationEvent(
        subjectId = subjectId,
        projectId = projectId,
        moduleId = moduleId,
        attendantId = attendantId,
        biometricReferences = EnrolmentRecordCreationEvent.buildBiometricReferences(samples, encoder),
        externalCredentials = externalCredentials,
    )

    companion object {
        val coSyncSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableStringV1::class.java, TokenizableStringV1Serializer())
            addDeserializer(TokenizableStringV1::class.java, TokenizableStringV1Deserializer())
        }
    }
}
