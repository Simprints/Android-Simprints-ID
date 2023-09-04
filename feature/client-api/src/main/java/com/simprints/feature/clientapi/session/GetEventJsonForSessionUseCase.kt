package com.simprints.feature.clientapi.session

import com.simprints.core.tools.json.JsonHelper
import com.simprints.feature.clientapi.models.CoSyncEvents
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.canCoSyncAllData
import com.simprints.infra.config.domain.models.canCoSyncAnalyticsData
import com.simprints.infra.config.domain.models.canCoSyncBiometricData
import com.simprints.infra.config.domain.models.canCoSyncData
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.EnrolmentEventV2
import com.simprints.infra.events.event.domain.models.PersonCreationEvent
import com.simprints.infra.events.event.domain.models.face.FaceCaptureBiometricsEvent
import com.simprints.infra.events.event.domain.models.fingerprint.FingerprintCaptureBiometricsEvent
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.toList
import javax.inject.Inject

internal class GetEventJsonForSessionUseCase @Inject constructor(
    private val configManager: ConfigManager,
    private val eventRepository: EventRepository,
    private val jsonHelper: JsonHelper,
) {
    suspend operator fun invoke(sessionId: String): String? {
        val config = configManager.getProjectConfiguration()

        if (!config.canCoSyncData()) {
            return null
        }

        val events = when {
            config.canCoSyncAllData() -> eventRepository.observeEventsFromSession(sessionId)

            config.canCoSyncBiometricData() -> eventRepository.observeEventsFromSession(sessionId)
                .filter { it is EnrolmentEventV2 || it is PersonCreationEvent || it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }

            config.canCoSyncAnalyticsData() -> eventRepository.observeEventsFromSession(sessionId)
                .filterNot { it is FingerprintCaptureBiometricsEvent || it is FaceCaptureBiometricsEvent }

            else -> emptyFlow()
        }
        return jsonHelper.toJson(CoSyncEvents(events.toList()))
    }
}
