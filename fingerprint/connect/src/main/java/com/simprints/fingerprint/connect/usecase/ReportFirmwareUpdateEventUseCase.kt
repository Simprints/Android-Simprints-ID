package com.simprints.fingerprint.connect.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.fingerprint.infra.scanner.domain.ota.AvailableOta
import com.simprints.infra.events.event.domain.models.ScannerFirmwareUpdateEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ReportFirmwareUpdateEventUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(
        startTime: Timestamp,
        availableOta: AvailableOta,
        targetVersions: String,
        e: Throwable? = null,
    ) {
        sessionCoroutineScope.launch {
            val chipName = when (availableOta) {
                AvailableOta.CYPRESS -> "cypress"
                AvailableOta.STM -> "stm"
                AvailableOta.UN20 -> "un20"
            }
            val failureReason = e?.let { "${it::class.java.simpleName} : ${it.message}" }

            eventRepository.addOrUpdateEvent(
                ScannerFirmwareUpdateEvent(
                    startTime,
                    timeHelper.now(),
                    chipName,
                    targetVersions,
                    failureReason,
                ),
            )
        }
    }
}
