package com.simprints.fingerprint.connect.usecase

import com.simprints.core.SessionCoroutineScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import com.simprints.infra.events.session.SessionEventRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ReportAlertScreenEventUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: SessionEventRepository,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
) {
    operator fun invoke(eventType: AlertScreenEventType) {
        sessionCoroutineScope.launch {
            eventRepository.addOrUpdateEvent(AlertScreenEvent(timeHelper.now(), eventType))
        }
    }

    fun reportBluetoothNotEnabled() = this(AlertScreenEventType.BLUETOOTH_NOT_ENABLED)

    fun reportNfcNotEnabled() = this(AlertScreenEventType.NFC_NOT_ENABLED)

    fun reportNfcPairing() = this(AlertScreenEventType.NFC_PAIR)

    fun reportSerialEntry() = this(AlertScreenEventType.SERIAL_ENTRY_PAIR)

    fun reportScannerOff() = this(AlertScreenEventType.DISCONNECTED)

    fun reportOta() = this(AlertScreenEventType.OTA)

    fun reportOtaFailed() = this(AlertScreenEventType.OTA_FAILED)

    fun reportOtaRecovery() = this(AlertScreenEventType.OTA_RECOVERY)
}
