package com.simprints.feature.fetchsubject.screen

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertButtonConfig
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R

internal object FetchSubjectAlerts {

    const val ACTION_CLOSE = "action_close"
    const val ACTION_RETRY = "action_retry"

    private const val WAS_ONLINE = "was_online"

    fun subjectNotFoundOnline() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = R.string.fetch_subject_guid_not_found_alert_title
        image = R.drawable.ic_alert_default
        messageRes = R.string.fetch_subject_guid_not_found_online_message
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.GUID_NOT_FOUND_ONLINE

        leftButton =  AlertButtonConfig.Close.copy(resultKey = ACTION_CLOSE)

        payload = bundleOf(WAS_ONLINE to true)
    }

    fun subjectNotFoundOffline() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = R.string.fetch_subject_guid_not_found_alert_title
        image = R.drawable.ic_alert_default
        messageRes = R.string.fetch_subject_guid_not_found_offline_message
        messageIcon = R.drawable.ic_alert_hint_no_network
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE

        leftButton = alertButton {
            textRes = R.string.fetch_subject_try_again_button
            closeOnClick = true
            resultKey = ACTION_RETRY
        }
        rightButton = AlertButtonConfig.Close.copy(resultKey = ACTION_CLOSE)

        payload = bundleOf(WAS_ONLINE to false)
    }

    fun wasOnline(bundle: Bundle) = bundle.getBoolean(WAS_ONLINE, false)

}
