package com.simprints.eventsystem.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.eventsystem.event.domain.models.session.Location
import com.simprints.eventsystem.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.eventsystem.event.remote.ApiModes
import com.simprints.eventsystem.event.remote.fromDomainToApi
import com.simprints.eventsystem.event.remote.models.ApiEventPayload
import com.simprints.eventsystem.event.remote.models.ApiEventPayloadType
import java.util.*


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiSessionCapturePayload(override val version: Int,
                                    val id: String = UUID.randomUUID().toString(),
                                    val projectId: String,
                                    override val startTime: Long,
                                    val endTime: Long,
                                    var uploadTime: Long,
                                    val modalities: List<ApiModes>,
                                    val appVersionName: String,
                                    val libVersionName: String,
                                    val analyticsId: String? = null,
                                    val language: String,
                                    val device: ApiDevice,
                                    val databaseInfo: ApiDatabaseInfo,
                                    val location: ApiLocation? = null) : ApiEventPayload(ApiEventPayloadType.SessionCapture, version, startTime) {

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
        domainPayload.analyticsId,
        domainPayload.language,
        domainPayload.device.fromDomainToApi(),
        domainPayload.databaseInfo.fromDomainToApi(),
        domainPayload.location.fromDomainToApi()
    )
}

fun Location?.fromDomainToApi() =
    this?.let {
        ApiLocation(latitude, longitude)
    }
