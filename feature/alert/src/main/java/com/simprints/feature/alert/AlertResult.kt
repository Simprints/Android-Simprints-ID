package com.simprints.feature.alert

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AlertResult(
    val buttonKey: String,
    val payload: Bundle,
) : Parcelable {

    fun isBackButtonPress() = buttonKey == AlertContract.ALERT_BUTTON_PRESSED_BACK
}
