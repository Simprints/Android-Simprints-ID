package com.simprints.feature.clientapi.usecases

import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameDeserializer
import com.simprints.core.domain.tokenization.serialization.TokenizationClassNameSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.core.tools.utils.EncodingUtils
import com.simprints.infra.config.store.models.canCoSyncAllData
import com.simprints.infra.config.store.models.canCoSyncBiometricData
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.infra.enrolment.records.repository.EnrolmentRecordRepository
import com.simprints.infra.enrolment.records.repository.domain.models.Subject
import com.simprints.infra.enrolment.records.repository.domain.models.SubjectQuery
import com.simprints.infra.events.event.cosync.CoSyncEnrolmentRecordEvents
import com.simprints.infra.events.event.domain.models.subject.EnrolmentRecordCreationEvent
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
            ?: return null

        return jsonHelper.toJson(CoSyncEnrolmentRecordEvents(listOf(recordCreationEvent)), coSyncSerializationModule)
    }

    private fun Subject.fromSubjectToEnrolmentCreationEvent() = EnrolmentRecordCreationEvent(
        subjectId,
        projectId,
        moduleId,
        attendantId,
        EnrolmentRecordCreationEvent.buildBiometricReferences(fingerprintSamples, faceSamples, encoder),
    )

    companion object {
        val coSyncSerializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationClassNameSerializer())
            addDeserializer(TokenizableString::class.java, TokenizationClassNameDeserializer())
        }
    }
}
