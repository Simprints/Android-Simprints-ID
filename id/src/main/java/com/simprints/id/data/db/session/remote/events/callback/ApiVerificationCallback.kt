package com.simprints.id.data.db.session.remote.events.callback

import androidx.annotation.Keep

@Keep
class ApiVerificationCallback(val score: ApiCallbackComparisonScore): ApiCallback(ApiCallbackType.VERIFICATION)
