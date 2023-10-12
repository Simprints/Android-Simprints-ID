package com.simprints.infra.eventsync.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.session.Location
import com.simprints.infra.events.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayloadType
import com.simprints.infra.eventsync.event.remote.models.ApiModality
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi
import java.util.*

@Keep
@JsonInclude(Include.NON_NULL)
internal data class ApiSessionCapturePayload(
    override val version: Int,
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    override val startTime: Long,
    val endTime: Long,
    var uploadTime: Long,
    val modalities: List<ApiModality>,
    val appVersionName: String,
    val libVersionName: String,
    val language: String,
    val device: ApiDevice,
    val databaseInfo: ApiDatabaseInfo,
    val location: ApiLocation? = null
) : ApiEventPayload(ApiEventPayloadType.SessionCapture, version, startTime) {

    constructor(domainPayload: SessionCapturePayload) : this(
        domainPayload.eventVersion,
        domainPayload.id,
        domainPayload.projectId,
        domainPayload.createdAt,
        domainPayload.endedAt,
        domainPayload.uploadedAt,
        domainPayload.modalities.map { it.fromDomainToApi() },
        domainPayload.appVersionName,
        domainPayload.libVersionName,
        domainPayload.language,
        domainPayload.device.fromDomainToApi(),
        domainPayload.databaseInfo.fromDomainToApi(),
        domainPayload.location.fromDomainToApi()
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? =
        null // this payload doesn't have tokenizable fields
}

internal fun Location?.fromDomainToApi() =
    this?.let {
        ApiLocation(latitude, longitude)
    }
