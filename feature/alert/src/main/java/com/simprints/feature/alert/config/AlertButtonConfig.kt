package com.simprints.feature.alert.config

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.feature.alert.AlertContract
import java.io.Serializable
import com.simprints.infra.resources.R as IDR

@Keep
data class AlertButtonConfig(
    val text: String?,
    @StringRes val textRes: Int?,
    val resultKey: String?,
    val closeOnClick: Boolean,
) : Serializable {
    companion object {
        val Close = AlertButtonConfig(
            text = null,
            textRes = IDR.string.alert_button_close,
            resultKey = AlertContract.ALERT_BUTTON_PRESSED_BACK,
            closeOnClick = true,
        )
    }
}
