package com.simprints.fingerprint.connect.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.fingerprint.infra.scanner.ScannerManager
import com.simprints.fingerprint.infra.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.infra.scanner.wrapper.ScannerWrapper
import com.simprints.infra.events.event.domain.models.ScannerConnectionEvent
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SaveScannerConnectionEventsUseCase @Inject constructor(
    private val scannerManager: ScannerManager,
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke() {
        sessionCoroutineScope.launch {
            val scanner = scannerManager.scanner

            eventRepository.addOrUpdateEvent(
                ScannerConnectionEvent(
                    timeHelper.now(),
                    ScannerConnectionEvent.ScannerConnectionPayload.ScannerInfo(
                        scannerManager.currentScannerId ?: "",
                        scannerManager.currentMacAddress ?: "",
                        scannerGeneration(scanner),
                        scanner.hardwareVersion(),
                    ),
                ),
            )
            if (scanner.versionInformation().generation == ScannerGeneration.VERO_2) {
                eventRepository.addOrUpdateEvent(
                    Vero2InfoSnapshotEvent(
                        timeHelper.now(),
                        scanner.versionInformation().let {
                            Vero2InfoSnapshotEvent.Vero2Version.Vero2NewApiVersion(
                                cypressApp = it.firmware.cypress,
                                stmApp = it.firmware.stm,
                                un20App = it.firmware.un20,
                                hardwareRevision = it.hardwareVersion,
                            )
                        },
                        scanner.batteryInformation().let {
                            Vero2InfoSnapshotEvent.BatteryInfo(it.charge, it.voltage, it.current, it.temperature)
                        },
                    ),
                )
            }
        }
    }

    private fun scannerGeneration(scanner: ScannerWrapper) = when (scanner.versionInformation().generation) {
        ScannerGeneration.VERO_1 -> ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_1
        ScannerGeneration.VERO_2 -> ScannerConnectionEvent.ScannerConnectionPayload.ScannerGeneration.VERO_2
    }

    private fun ScannerWrapper.hardwareVersion() = when (versionInformation().generation) {
        ScannerGeneration.VERO_1 -> versionInformation().firmware.stm
        ScannerGeneration.VERO_2 -> versionInformation().hardwareVersion
    }
}
