package com.simprints.feature.alert.config

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.feature.alert.AlertContract
import kotlinx.serialization.Serializable
import com.simprints.infra.resources.R as IDR

@Keep
@Serializable
data class AlertButtonConfig(
    val text: String?,
    @StringRes val textRes: Int?,
    val resultKey: String?,
    val closeOnClick: Boolean,
) {
    companion object {
        val Close = AlertButtonConfig(
            text = null,
            textRes = IDR.string.alert_button_close,
            resultKey = AlertContract.ALERT_BUTTON_PRESSED_BACK,
            closeOnClick = true,
        )
    }
}
