package com.simprints.feature.orchestrator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.simprints.infra.logging.Simber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ExecutionTracker @Inject constructor() : LifecycleEventObserver {

    /**
     * There have been a number of cases when calling app makes several requests tens of ms apart, while it
     * is not exactly clear why this is happening, it is clear that it is not the intended behavior.
     *
     * This in-memory flag is used to prevent the orchestrator from starting multiple times.
     * It will cause any consecutive requests to return `Activity.RESULT_CANCELED` and it is callers
     * responsibility to handle this case.
     */
    val isExecuting = AtomicBoolean(false)

    private var activityCount = 0

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_CREATE) {
            activityCount++
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            activityCount = (activityCount - 1).coerceAtLeast(0)
            if (activityCount == 0) {
                isExecuting.set(false)
                Simber.i("All activities have been closed.")
            }
        }

    }
}
