package com.simprints.fingerprint.connect.usecase

import com.simprints.core.ExternalScope
import com.simprints.core.tools.time.TimeHelper
import com.simprints.infra.events.EventRepository
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.events.event.domain.models.AlertScreenEvent.AlertScreenPayload.AlertScreenEventType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class ReportAlertScreenEventUseCase @Inject constructor(
    private val timeHelper: TimeHelper,
    private val eventRepository: EventRepository,
    @ExternalScope private val externalScope: CoroutineScope,
) {

    operator fun invoke(eventType: AlertScreenEventType) {
        externalScope.launch {
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
