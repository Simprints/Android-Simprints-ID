package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.DeviceConfigurationUpdatedEvent
import com.simprints.infra.events.event.domain.models.DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdateSource
import com.simprints.infra.eventsync.event.remote.models.ApiDeviceConfigurationUpdatedPayload.ApiDeviceConfigurationUpdateSource.LOCAL
import com.simprints.infra.eventsync.event.remote.models.ApiDeviceConfigurationUpdatedPayload.ApiDeviceConfigurationUpdateSource.REMOTE
import kotlinx.serialization.Serializable

@Keep
@Serializable
internal data class ApiDeviceConfigurationUpdatedPayload(
    override val startTime: ApiTimestamp,
    val configuration: ApiDeviceConfigurationUpdated,
    val sourceUpdate: ApiDeviceConfigurationUpdateSource,
) : ApiEventPayload() {
    constructor(domainPayload: DeviceConfigurationUpdatedEvent.DeviceConfigurationUpdatedPayload) : this(
        startTime = domainPayload.createdAt.fromDomainToApi(),
        configuration = ApiDeviceConfigurationUpdated(
            language = domainPayload.configuration.language,
            downSyncModules = domainPayload.configuration.downSyncModules?.map { it.value },
        ),
        sourceUpdate = when (domainPayload.sourceUpdate) {
            DeviceConfigurationUpdateSource.REMOTE -> REMOTE
            DeviceConfigurationUpdateSource.LOCAL -> LOCAL
        },
    )

    @Keep
    @Serializable
    data class ApiDeviceConfigurationUpdated(
        val language: String,
        val downSyncModules: List<String>? = null,
    )

    @Serializable
    enum class ApiDeviceConfigurationUpdateSource {
        REMOTE,
        LOCAL,
    }

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.ModuleId -> "configuration.downSyncModules"
        else -> null
    }
}
