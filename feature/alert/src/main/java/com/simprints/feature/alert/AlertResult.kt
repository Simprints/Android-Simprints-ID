package com.simprints.feature.alert

import android.os.Bundle
import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class AlertResult(
    val buttonKey: String,
    val payload: Bundle,
) : Serializable
