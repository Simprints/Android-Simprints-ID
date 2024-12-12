package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.BatteryInfo
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2InfoSnapshotPayload
import com.simprints.infra.events.event.domain.models.Vero2InfoSnapshotEvent.Vero2Version

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "version",
    visible = true,
)
@JsonSubTypes(
    JsonSubTypes.Type(
        value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi::class,
        name = Vero2InfoSnapshotEvent.NEW_EVENT_VERSION.toString(),
    ),
    JsonSubTypes.Type(
        value = Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi::class,
        name = Vero2InfoSnapshotEvent.OLD_EVENT_VERSION.toString(),
    ),
)
@Keep
internal sealed class ApiVero2InfoSnapshotPayload(
    override val startTime: ApiTimestamp,
    open val scannerVersion: ApiVero2Version,
    open val battery: ApiBatteryInfo,
) : ApiEventPayload(startTime) {
    data class ApiVero2InfoSnapshotPayloadForNewApi(
        override val startTime: ApiTimestamp,
        override val scannerVersion: ApiVero2Version,
        override val battery: ApiBatteryInfo,
    ) : ApiVero2InfoSnapshotPayload(
            startTime,
            scannerVersion,
            battery,
        ) {
        override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
    }

    @Deprecated(message = "used only for backwards compatibility")
    data class ApiVero2InfoSnapshotPayloadForOldApi(
        override val startTime: ApiTimestamp,
        override val scannerVersion: ApiVero2Version,
        override val battery: ApiBatteryInfo,
    ) : ApiVero2InfoSnapshotPayload(
            startTime,
            scannerVersion,
            battery,
        ) {
        override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = null // this payload doesn't have tokenizable fields
    }

    sealed class ApiVero2Version {
        @Keep
        data class ApiNewVero2Version(
            val hardwareRevision: String,
            val cypressApp: String,
            val stmApp: String,
            val un20App: String,
            val master: Long,
        ) : ApiVero2Version() {
            constructor(vero2Version: Vero2Version.Vero2NewApiVersion) :
                this(
                    vero2Version.hardwareRevision,
                    vero2Version.cypressApp,
                    vero2Version.stmApp,
                    vero2Version.un20App,
                    vero2Version.master,
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
            val un20Api: String,
        ) : ApiVero2Version() {
            constructor(vero2Version: Vero2Version.Vero2OldApiVersion) :
                this(
                    vero2Version.master,
                    vero2Version.cypressApp,
                    vero2Version.cypressApi,
                    vero2Version.stmApp,
                    vero2Version.stmApi,
                    vero2Version.un20App,
                    vero2Version.un20Api,
                )
        }
    }

    @Keep
    data class ApiBatteryInfo(
        val charge: Int,
        val voltage: Int,
        val current: Int,
        val temperature: Int,
    ) {
        constructor(batteryInfo: BatteryInfo) :
            this(
                batteryInfo.charge,
                batteryInfo.voltage,
                batteryInfo.current,
                batteryInfo.temperature,
            )
    }
}

private fun Vero2Version.toApiVero2Version() = when (this) {
    is Vero2Version.Vero2OldApiVersion ->
        ApiVero2InfoSnapshotPayload.ApiVero2Version.ApiOldVero2Version(this)
    is Vero2Version.Vero2NewApiVersion ->
        ApiVero2InfoSnapshotPayload.ApiVero2Version.ApiNewVero2Version(this)
}

internal fun toApiVero2InfoSnapshotPayload(domainPayload: Vero2InfoSnapshotPayload): ApiVero2InfoSnapshotPayload = when (domainPayload) {
    is Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForNewApi -> {
        ApiVero2InfoSnapshotPayload.ApiVero2InfoSnapshotPayloadForNewApi(
            domainPayload.createdAt.fromDomainToApi(),
            domainPayload.version.toApiVero2Version(),
            ApiVero2InfoSnapshotPayload.ApiBatteryInfo(domainPayload.battery),
        )
    }
    is Vero2InfoSnapshotPayload.Vero2InfoSnapshotPayloadForOldApi -> {
        ApiVero2InfoSnapshotPayload.ApiVero2InfoSnapshotPayloadForOldApi(
            domainPayload.createdAt.fromDomainToApi(),
            domainPayload.version.toApiVero2Version(),
            ApiVero2InfoSnapshotPayload.ApiBatteryInfo(domainPayload.battery),
        )
    }
}
