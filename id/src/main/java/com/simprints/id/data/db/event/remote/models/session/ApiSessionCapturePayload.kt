package com.simprints.id.data.db.event.remote.models.session

import androidx.annotation.Keep
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.simprints.id.data.db.event.domain.models.session.Location
import com.simprints.id.data.db.event.domain.models.session.SessionCaptureEvent.SessionCapturePayload
import com.simprints.id.data.db.event.remote.ApiModes
import com.simprints.id.data.db.event.remote.fromDomainToApi
import com.simprints.id.data.db.event.remote.models.ApiEventPayload
import com.simprints.id.data.db.event.remote.models.ApiEventPayloadType
import java.util.*


@Keep
@JsonInclude(Include.NON_NULL)
data class ApiSessionCapturePayload(override val version: Int,
                                    val id: String = UUID.randomUUID().toString(),
                                    val projectId: String,
                                    val startTime: Long,
                                    val serverStartTime: Long,
                                    val relativeEndTime: Long,
                                    val relativeUploadTime: Long,
                                    val modalities: List<ApiModes>,
                                    val appVersionName: String,
                                    val libVersionName: String,
                                    val analyticsId: String? = null,
                                    val language: String,
                                    val device: ApiDevice,
                                    val databaseInfo: ApiDatabaseInfo,
                                    val location: ApiLocation? = null,
                                    @JsonIgnore override val relativeStartTime: Long = 0) : ApiEventPayload(ApiEventPayloadType.SessionCapture, version, relativeStartTime) {

    constructor(domainPayload: SessionCapturePayload) : this(
        domainPayload.eventVersion,
        domainPayload.id,
        domainPayload.projectId,
        domainPayload.createdAt,
        domainPayload.serverStartTime, //StopShip: to fix timestamps
        domainPayload.endedAt,
        domainPayload.relativeUploadTime,
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
