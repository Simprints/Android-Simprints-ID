package com.simprints.feature.alert

import android.os.Bundle

object AlertContract {

    const val ALERT_REQUEST = "alert_fragment_request"
    const val ALERT_BUTTON_PRESSED = "alert_fragment_button"
    const val ALERT_BUTTON_PRESSED_BACK = "alert_fragment_back"
    const val ALERT_PAYLOAD = "alert_fragment_payload"

    /**
     * @return true if provided bundle contains provided action key.
     */
    fun hasResponseKey(data: Bundle, key: String) = data.getString(ALERT_BUTTON_PRESSED) == key

    /**
     * @return key of the pressed action button from the provided bundle. Defaults to empty string.
     */
    fun getResponseKey(data: Bundle): String = data.getString(ALERT_BUTTON_PRESSED, "")

    /**
     * @return the payload bundle exactly as it was provided when assembling alert configuration.
     */
    fun getResponsePayload(data: Bundle) = data.getBundle(ALERT_PAYLOAD) ?: Bundle()
}
