package com.simprints.id.data.db.event.domain.models

import androidx.annotation.Keep

import com.simprints.id.data.db.event.domain.models.EventType.SCANNER_CONNECTION
import com.simprints.id.data.db.event.local.models.DbEvent.Companion.DEFAULT_EVENT_VERSION
import java.util.*

@Keep
data class ScannerConnectionEvent(
    override val id: String = UUID.randomUUID().toString(),
    override var labels: EventLabels,
    override val payload: ScannerConnectionPayload,
    override val type: EventType
) : Event() {

    constructor(
        createdAt: Long,
        scannerInfo: ScannerConnectionPayload.ScannerInfo,
        labels: EventLabels = EventLabels() //StopShip: to change in PAS-993
    ) : this(
        UUID.randomUUID().toString(),
        labels,
        ScannerConnectionPayload(createdAt, EVENT_VERSION, scannerInfo),
        SCANNER_CONNECTION)


    @Keep
    data class ScannerConnectionPayload(
        override val createdAt: Long,
        override val eventVersion: Int,
        val scannerInfo: ScannerInfo,
        override val type: EventType = SCANNER_CONNECTION,
        override val endedAt: Long = 0) : EventPayload() {

        @Keep
        data class ScannerInfo(val scannerId: String,
                               val macAddress: String,
                               val generation: ScannerGeneration,
                               var hardwareVersion: String?)

        enum class ScannerGeneration {
            VERO_1,
            VERO_2
        }
    }

    companion object {
        const val EVENT_VERSION = DEFAULT_EVENT_VERSION
    }
}
