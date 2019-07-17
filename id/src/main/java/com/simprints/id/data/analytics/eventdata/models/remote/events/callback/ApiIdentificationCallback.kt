package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import androidx.annotation.Keep
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallback
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackComparisonScore
import com.simprints.id.data.analytics.eventdata.models.remote.events.callback.ApiCallbackType

@Keep
class ApiIdentificationCallback(val sessionId: String,
                                val scores: List<ApiCallbackComparisonScore>): ApiCallback(ApiCallbackType.IDENTIFICATION)
