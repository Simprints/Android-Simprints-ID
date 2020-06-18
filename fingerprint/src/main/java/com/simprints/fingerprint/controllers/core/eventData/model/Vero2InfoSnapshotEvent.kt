package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.scanner.domain.versions.ScannerVersion
import com.simprints.fingerprint.scanner.domain.BatteryInfo as BatteryInfoDomain
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
    ) {
        companion object {
            fun get(scannerVersion: ScannerVersion) =
                with(scannerVersion) {
                    Vero2Version(
                        master = computeMasterVersion(),
                        cypressApp = firmware.cypress.toString(),
                        cypressApi = api.cypress.toString(),
                        stmApp = firmware.stm.toString(),
                        stmApi = api.stm.toString(),
                        un20App = firmware.un20.toString(),
                        un20Api = api.un20.toString()
                    )
                }
        }
    }

    @Keep
    class BatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int
    ) {
        companion object {
            fun get(batteryInfo: BatteryInfoDomain) =
                with(batteryInfo) {
                    BatteryInfo(charge, voltage, current, temperature)
                }
        }
    }
}

fun Vero2InfoSnapshotEvent.fromDomainToCore(): Vero2InfoSnapshotEventCore =
    Vero2InfoSnapshotEventCore(startTime, version.fromDomainToCore(), battery.fromDomainToCore())

fun Vero2InfoSnapshotEvent.Vero2Version.fromDomainToCore(): Vero2VersionCore =
    Vero2VersionCore(master, cypressApp, cypressApi, stmApp, stmApi, un20App, un20Api)

fun Vero2InfoSnapshotEvent.BatteryInfo.fromDomainToCore(): BatteryInfoCore =
    BatteryInfoCore(charge, voltage, current, temperature)
