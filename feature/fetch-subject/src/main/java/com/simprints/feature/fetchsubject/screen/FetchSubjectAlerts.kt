package com.simprints.feature.fetchsubject.screen

import android.os.Bundle
import androidx.core.os.bundleOf
import com.simprints.feature.alert.alertButton
import com.simprints.feature.alert.alertConfiguration
import com.simprints.feature.alert.config.AlertColor
import com.simprints.infra.events.event.domain.models.AlertScreenEvent
import com.simprints.infra.resources.R

internal object FetchSubjectAlerts {

    const val ACTION_CLOSE = "action_close"
    const val ACTION_RETRY = "action_retry"

    private const val PAYLOAD_EXIT_FORM_ON_BACK = "show_exit_form_on_back"
    const val IS_ONLINE = "is_online"

    fun subjectNotFoundOnline() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = R.string.verify_guid_not_found_title
        image = R.drawable.ic_alert_default
        messageRes = R.string.verify_guid_not_found_online_message
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.GUID_NOT_FOUND_ONLINE

        leftButton = alertButton {
            textRes = R.string.close
            closeOnClick = true
            resultKey = ACTION_CLOSE
        }

        payload = bundleOf(
            PAYLOAD_EXIT_FORM_ON_BACK to true,
            IS_ONLINE to true
        )
    }

    fun subjectNotFoundOffline() = alertConfiguration {
        color = AlertColor.Gray
        titleRes = R.string.verify_guid_not_found_title
        image = R.drawable.ic_alert_default
        messageRes = R.string.verify_guid_not_found_offline_message
        messageIcon = R.drawable.ic_alert_hint_no_network
        eventType = AlertScreenEvent.AlertScreenPayload.AlertScreenEventType.GUID_NOT_FOUND_OFFLINE

        leftButton = alertButton {
            textRes = R.string.try_again_label
            closeOnClick = true
            resultKey = ACTION_RETRY
        }
        rightButton = alertButton {
            textRes = R.string.close
            closeOnClick = true
            resultKey = ACTION_CLOSE
        }

        payload = bundleOf(
            PAYLOAD_EXIT_FORM_ON_BACK to true,
            IS_ONLINE to false
        )
    }

    fun shouldShowExitForm(bundle: Bundle) = bundle.getBoolean(PAYLOAD_EXIT_FORM_ON_BACK, false)

}
