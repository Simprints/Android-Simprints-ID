package com.simprints.fingerprint.activities.collect.state

sealed class LiveFeedbackState {
    object Start: LiveFeedbackState()
    object Pause: LiveFeedbackState()
    object Stop: LiveFeedbackState()
}
