package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.SCANNER_FIRMWARE_UPDATE
import java.util.*

@Keep
data class ScannerFirmwareUpdateEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ScannerFirmwareUpdatePayload,
    override val type: EventType
) : Event(id, labels, payload, type) {

    constructor(createdAt: Long,
                endTime: Long,
                chip: String,
                targetAppVersion: String,
                failureReason: String? = null,
                labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ScannerFirmwareUpdatePayload(createdAt, EVENT_VERSION, endTime, chip, targetAppVersion, failureReason),
        SCANNER_FIRMWARE_UPDATE)


    @Keep
    data class ScannerFirmwareUpdatePayload(override val createdAt: Long,
                                            override val eventVersion: Int,
                                            override var endedAt: Long,
                                            val chip: String,
                                            val targetAppVersion: String,
                                            var failureReason: String? = null)
        : EventPayload(SCANNER_FIRMWARE_UPDATE, eventVersion, createdAt, endedAt)

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
