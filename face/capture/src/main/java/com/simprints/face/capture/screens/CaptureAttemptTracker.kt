package com.simprints.face.capture.screens

import dagger.hilt.android.scopes.ActivityRetainedScoped
import javax.inject.Inject

@ActivityRetainedScoped
internal class CaptureAttemptTracker @Inject constructor() {
    // The attempt number to hand out to the next capture attempt that starts
    private var nextAttemptNumber: Int = 0

    var attemptNumber: Int = 0
        private set

    fun onNewCaptureAttemptStarted() {
        attemptNumber = nextAttemptNumber
        nextAttemptNumber++
    }

    fun reset() {
        attemptNumber = 0
        nextAttemptNumber = 0
    }
}
