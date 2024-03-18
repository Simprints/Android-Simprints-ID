package com.simprints.feature.alert

import android.os.Bundle
import androidx.annotation.Keep
import com.simprints.core.domain.response.AppErrorReason
import java.io.Serializable

@Keep
data class AlertResult(
    val buttonKey: String,
    val payload: Bundle,
) : Serializable {

    fun appErrorReason(): AppErrorReason = payload
        .getString(AlertContract.ALERT_REASON_PAYLOAD)
        ?.let { reason -> enumValues<AppErrorReason>().firstOrNull { it.name == reason } }
        ?: AppErrorReason.UNEXPECTED_ERROR
}
