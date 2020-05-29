package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.id.data.db.session.domain.models.events.Vero2InfoSnapshotEvent as Vero2InfoSnapshotEventCore
import com.simprints.id.data.db.session.domain.models.events.Vero2InfoSnapshotEvent.BatteryInfo as BatteryInfoCore
import com.simprints.id.data.db.session.domain.models.events.Vero2InfoSnapshotEvent.Vero2Version as Vero2VersionCore

@Keep
class Vero2InfoSnapshotEvent(startTime: Long,
                             val version: Vero2Version,
                             val battery: BatteryInfo) : Event(EventType.VERO_2_INFO_SNAPSHOT, startTime) {

    @Keep
    class Vero2Version(
        val master: Long,
        val cypressApp: String,
        val cypressApi: String,
        val stmApp: String,
        val stmApi: String,
        val un20App: String,
        val un20Api: String
    )

    @Keep
    class BatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int
    )
}

fun Vero2InfoSnapshotEvent.fromDomainToCore(): Vero2InfoSnapshotEventCore =
    Vero2InfoSnapshotEventCore(startTime, version.fromDomainToCore(), battery.fromDomainToCore())

fun Vero2InfoSnapshotEvent.Vero2Version.fromDomainToCore(): Vero2VersionCore =
    Vero2VersionCore(master, cypressApp, cypressApi, stmApp, stmApi, un20App, un20Api)

fun Vero2InfoSnapshotEvent.BatteryInfo.fromDomainToCore(): BatteryInfoCore =
    BatteryInfoCore(charge, voltage, current, temperature)
