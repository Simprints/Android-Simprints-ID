package com.simprints.fingerprint.controllers.core.eventData.model

import androidx.annotation.Keep
import com.simprints.fingerprint.controllers.core.eventData.model.EventType.*

/**
 * This enum class represents the different types of fingerprint events.
 *
 * - [REFUSAL_RESPONSE]  an event capturing the returned response form a refusal form
 * - [FINGERPRINT_CAPTURE]  an event capturing a fingerprint capture request
 * - [ONE_TO_ONE_MATCH]  an event capturing a verification match request
 * - [ONE_TO_MANY_MATCH]  an event capturing a identification match request
 * - [SCANNER_CONNECTION]  an event capturing a identification match request
 * - [ALERT_SCREEN]  an event capturing a identification match request
 * - [ALERT_SCREEN_WITH_SCANNER_ISSUE]  an event capturing a identification match request
 * - [VERO_2_INFO_SNAPSHOT]  an event capturing a identification match request
 * - [SCANNER_FIRMWARE_UPDATE]  an event capturing a identification match request
 */
@Keep
enum class EventType {
    REFUSAL_RESPONSE,
    FINGERPRINT_CAPTURE,
    FINGERPRINT_CAPTURE_BIOMETRICS,
    ONE_TO_ONE_MATCH,
    ONE_TO_MANY_MATCH,
    REFUSAL,
    SCANNER_CONNECTION,
    ALERT_SCREEN,
    ALERT_SCREEN_WITH_SCANNER_ISSUE,
    VERO_2_INFO_SNAPSHOT,
    SCANNER_FIRMWARE_UPDATE
}
