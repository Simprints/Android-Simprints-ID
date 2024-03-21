package com.simprints.feature.alert.config

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class AlertConfiguration(
    val color: AlertColor,
    val title: String?,
    @StringRes val titleRes: Int?,
    @DrawableRes val image: Int,
    val message: String?,
    @StringRes val messageRes: Int?,
    @DrawableRes val messageIcon: Int?,
    val leftButton: AlertButtonConfig,
    val rightButton: AlertButtonConfig?,
    val eventType: AlertScreenEvent.AlertScreenPayload.AlertScreenEventType?,
    val appErrorReason: AppErrorReason? = null,
) : Parcelable
