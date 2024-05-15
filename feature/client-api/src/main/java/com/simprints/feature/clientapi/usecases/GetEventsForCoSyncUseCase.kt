package com.simprints.feature.clientapi.usecases

import com.fasterxml.jackson.databind.module.SimpleModule
import com.simprints.core.domain.tokenization.TokenizableString
import com.simprints.core.domain.tokenization.serialization.TokenizationAsStringSerializer
import com.simprints.core.tools.json.JsonHelper
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.Project
import com.simprints.infra.config.store.models.canCoSyncAllData
import com.simprints.infra.config.store.models.canCoSyncAnalyticsData
import com.simprints.infra.config.store.models.canCoSyncBiometricData
import com.simprints.infra.config.store.models.canCoSyncData
import com.simprints.infra.config.store.tokenization.TokenizationProcessor
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.cosync.CoSyncEvents
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.Event
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import javax.inject.Inject

internal class GetEventsForCoSyncUseCase @Inject constructor(
    private val configRepository: ConfigRepository,
    private val eventRepository: EventRepository,
    private val jsonHelper: JsonHelper,
    private val tokenizationProcessor: TokenizationProcessor,
) {

    suspend operator fun invoke(sessionId: String, project: Project?): String? {
        val config = configRepository.getProjectConfiguration()

        if (!config.canCoSyncData()) {
            return null
        }

        val events = when {
            config.canCoSyncAllData() -> eventRepository.getEventsFromScope(sessionId)

            config.canCoSyncBiometricData() -> eventRepository.getEventsFromScope(sessionId)
                .filter { it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }

            config.canCoSyncAnalyticsData() -> eventRepository.getEventsFromScope(sessionId)
                .filterNot { it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }

            else -> emptyList()
        }
        val decryptedEvents = decryptTokenizedFields(events = events, project = project)
        val serializationModule = SimpleModule().apply {
            addSerializer(TokenizableString::class.java, TokenizationAsStringSerializer())
        }
        return jsonHelper.toJson(CoSyncEvents(decryptedEvents), module = serializationModule)
    }

    private fun decryptTokenizedFields(events: List<Event>, project: Project?): List<Event> =
        events.map { event ->
            if (project == null) return@map event
            val decryptedFieldsMap = event.getTokenizedFields().mapValues { entry ->
                when (val value = entry.value) {
                    is TokenizableString.Raw -> value
                    is TokenizableString.Tokenized -> tokenizationProcessor.decrypt(
                        encrypted = value,
                        tokenKeyType = entry.key,
                        project = project
                    )
                }
            }
            return@map event.setTokenizedFields(decryptedFieldsMap)
        }
}
