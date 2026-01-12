package com.simprints.infra.eventsync.event.remote.models

import androidx.annotation.Keep
import com.simprints.infra.events.event.domain.models.EventType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
@SerialName(EventType.CALLBACK_VERIFICATION_KEY)
internal data class ApiVerificationCallback(
    val score: ApiCallbackComparisonScore,
    override val type: ApiCallbackType = ApiCallbackType.Verification,
) : ApiCallback()
