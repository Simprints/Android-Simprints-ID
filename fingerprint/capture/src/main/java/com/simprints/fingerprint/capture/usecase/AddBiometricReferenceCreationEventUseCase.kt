package com.simprints.fingerprint.capture.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.BiometricReferenceCreationEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class AddBiometricReferenceCreationEventUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(
        referenceId: String,
        captureIds: List<String>,
    ) = sessionCoroutineScope.launch {
        eventRepository.addOrUpdateEvent(
            BiometricReferenceCreationEvent(
                startTime = timeHelper.now(),
                referenceId = referenceId,
                modality = BiometricReferenceCreationEvent.BiometricReferenceModality.FINGERPRINT,
                captureIds = captureIds,
            ),
        )
    }
}
