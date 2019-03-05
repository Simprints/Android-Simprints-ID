package com.simprints.id.data.analytics.eventdata.models.remote.events

import com.simprints.id.data.analytics.eventdata.models.domain.events.ConsentEvent

class ApiConsentEvent(val relativeStartTime: Long,
                      var relativeEndTime: Long,
                      val consentType: ApiType,
                      var result: ApiResult) : ApiEvent(ApiEventType.CONSENT) {

    enum class ApiType {
        INDIVIDUAL, PARENTAL
    }

    enum class ApiResult {
        ACCEPTED, DECLINED, NO_RESPONSE
    }

    constructor(consentEvent: ConsentEvent) :
        this(consentEvent.relativeStartTime,
            consentEvent.relativeEndTime,
            ApiType.valueOf(consentEvent.consentType.toString()),
            ApiResult.valueOf(consentEvent.result.toString()))
}
