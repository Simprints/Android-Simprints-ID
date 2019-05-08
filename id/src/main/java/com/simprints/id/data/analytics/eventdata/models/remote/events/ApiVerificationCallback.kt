package com.simprints.id.data.analytics.eventdata.models.remote.events

import androidx.annotation.Keep

@Keep
class ApiVerificationCallback(val score: ApiCallbackComparisonScore): ApiCallback(ApiCallbackType.VERIFICATION)
