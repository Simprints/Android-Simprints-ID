package com.simprints.feature.alert.config

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize
import com.simprints.infra.resources.R as IDR

@Keep
@Parcelize
data class AlertButtonConfig(
    val text: String?,
    @StringRes val textRes: Int?,
    val resultKey: String?,
    val closeOnClick: Boolean,
) : Parcelable {

    companion object {

        val Close = AlertButtonConfig(
            text = null,
            textRes = IDR.string.close,
            resultKey = null,
            closeOnClick = true,
        )
    }
}
