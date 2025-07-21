package com.simprints.feature.alert.config

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.simprints.core.domain.response.AppErrorReason
import com.simprints.core.domain.step.StepParams
import com.simprints.infra.events.event.domain.models.AlertScreenEvent

@Keep
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
) : StepParams
