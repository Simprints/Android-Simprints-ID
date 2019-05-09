package com.simprints.id.data.analytics.eventdata.models.remote.events.callback

import io.realm.internal.Keep


@Keep
class ApiVerificationCallback(val score: ApiCallbackComparisonScore) : ApiCallback(ApiCallbackType.VERIFICATION)

