package com.simprints.eventsystem.event.remote.models

import androidx.annotation.Keep
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.BatteryInfo
import com.simprints.eventsystem.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload.Vero2Version


@Keep
data class ApiVero2InfoSnapshotPayload(
    override val startTime: Long,
    override val version: Int,
    val scannerVersion: ApiVero2Version,
    val battery: ApiBatteryInfo
): ApiEventPayload(ApiEventPayloadType.Vero2InfoSnapshot, version, startTime) {


    sealed class ApiVero2Version {

        @Keep
        data class ApiNewVero2Version(
            val hardwareVersion: String,
            val cypressApp: String,
            val stmApp: String,
            val un20App: String
        ): ApiVero2Version() {

            constructor(vero2Version: Vero2Version.Vero2NewApiVersion):
                this(
                    vero2Version.hardwareVersion,
                    vero2Version.cypressApp,
                    vero2Version.stmApp,
                    vero2Version.un20App
                )
        }


        @Deprecated(message = "used only for backwards compatibility")
        @Keep
        data class ApiOldVero2Version(
            val master: Long,
            val cypressApp: String,
            val cypressApi: String,
            val stmApp: String,
            val stmApi: String,
            val un20App: String,
            val un20Api: String
        ): ApiVero2Version() {

            constructor(vero2Version: Vero2Version.Vero2OldApiVersion):
                this(
                    vero2Version.master,
                    vero2Version.cypressApp,
                    vero2Version.cypressApi,
                    vero2Version.stmApp,
                    vero2Version.stmApi,
                    vero2Version.un20App,
                    vero2Version.un20Api
                )
        }
    }

    @Keep
    data class ApiBatteryInfo(
        val charge: Int, val voltage: Int,
        val current: Int, val temperature: Int
    ) {

        constructor(batteryInfo: BatteryInfo):
            this(
                batteryInfo.charge,
                batteryInfo.voltage,
                batteryInfo.current,
                batteryInfo.temperature
            )
    }

    constructor(domainPayload: Vero2InfoSnapshotPayload):
        this(
            domainPayload.createdAt,
            domainPayload.eventVersion,
            domainPayload.version.toApiVero2Version(),
            ApiBatteryInfo(domainPayload.battery)
        )
}


private fun Vero2Version.toApiVero2Version() = when(this){
    is Vero2Version.Vero2OldApiVersion ->
        ApiVero2InfoSnapshotPayload.ApiVero2Version.ApiOldVero2Version(this)
    is Vero2Version.Vero2NewApiVersion ->
        ApiVero2InfoSnapshotPayload.ApiVero2Version.ApiNewVero2Version(this)
}
