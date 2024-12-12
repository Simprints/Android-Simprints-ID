package com.simprints.infra.eventsync.event.remote.models.downsync

import androidx.annotation.Keep
import com.simprints.infra.config.store.models.TokenKeyType
import com.simprints.infra.events.event.domain.models.downsync.EventDownSyncRequestEvent
import com.simprints.infra.eventsync.event.remote.models.ApiEventPayload
import com.simprints.infra.eventsync.event.remote.models.ApiTimestamp
import com.simprints.infra.eventsync.event.remote.models.fromDomainToApi

@Keep
internal data class ApiEventDownSyncRequestPayload(
    override val startTime: ApiTimestamp,
    val endTime: ApiTimestamp?,
    val requestId: String,
    val queryParameters: ApiQueryParameters,
    val responseStatus: Int?,
    val errorType: String?,
    val msToFirstResponseByte: Long?,
    val eventsRead: Int?,
) : ApiEventPayload(startTime) {
    constructor(domainPayload: EventDownSyncRequestEvent.EventDownSyncRequestPayload) : this(
        domainPayload.createdAt.fromDomainToApi(),
        domainPayload.endedAt?.fromDomainToApi(),
        domainPayload.requestId,
        ApiQueryParameters(
            domainPayload.queryParameters.moduleId,
            domainPayload.queryParameters.attendantId,
            domainPayload.queryParameters.subjectId,
            domainPayload.queryParameters.modes,
            domainPayload.queryParameters.lastEventId,
        ),
        domainPayload.responseStatus,
        domainPayload.errorType,
        domainPayload.msToFirstResponseByte,
        domainPayload.eventsRead,
    )

    @Keep
    data class ApiQueryParameters(
        val moduleId: String?,
        val attendantId: String?,
        val subjectId: String?,
        val modes: List<String>?,
        val lastEventId: String?,
    )

    override fun getTokenizedFieldJsonPath(tokenKeyType: TokenKeyType): String? = when (tokenKeyType) {
        TokenKeyType.AttendantId -> "queryParameters.attendantId"
        TokenKeyType.ModuleId -> "queryParameters.moduleId"
        else -> null
    }
}
