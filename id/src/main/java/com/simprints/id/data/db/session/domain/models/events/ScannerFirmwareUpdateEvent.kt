package com.simprints.id.data.db.session.domain.models.events

import androidx.annotation.Keep

@Keep
class ScannerFirmwareUpdateEvent(startTime: Long,
                                 endTime: Long,
                                 val chip: String,
                                 val targetAppVersion: String,
                                 var failureReason: String? = null) : Event(EventType.SCANNER_FIRMWARE_UPDATE, startTime, endTime)

