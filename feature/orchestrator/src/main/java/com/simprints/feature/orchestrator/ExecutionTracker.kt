package com.simprints.feature.orchestrator

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.simprints.core.tools.time.TimeHelper
import com.simprints.core.tools.time.Timestamp
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.ORCHESTRATION
import com.simprints.infra.logging.Simber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ExecutionTracker @Inject constructor(
    private val timeHelper: TimeHelper,
    @ExecutorLockTimeoutSec private val executionTimeLimitSec: Int,
) : LifecycleEventObserver {
    /**
     * Timestamp representing when the [currentLifecycleOwnerId] was set. This is a safeguard
     * measure for cases when [Lifecycle.Event.ON_DESTROY] is not called by the system. Should it
     * ever happen, the [ExecutionTracker] has a check for whether enough time has passed, and if
     * the [currentLifecycleOwnerId] can be released.
     *
     * Limit for execution is set by the [executionTimeLimitSec] (in seconds).
     */
    private var timestamp: Timestamp = Timestamp(0)

    /**
     * ID of the [LifecycleOwner] that is considered the main executor.
     */
    private var currentLifecycleOwnerId: Int? = null

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
    ) {
        val ownerId = source.id()
        if (event == Lifecycle.Event.ON_CREATE) {
            if (currentLifecycleOwnerId == null || enoughTimePassedSinceLastLock()) {
                currentLifecycleOwnerId = ownerId
                timestamp = timeHelper.now()
                Simber.i("Lifecycle owner [$ownerId] is set as main executor", tag = ORCHESTRATION)
            } else {
                Simber.i("Main executor already set, ignoring Lifecycle owner [$ownerId]", tag = ORCHESTRATION)
            }
        } else if (event == Lifecycle.Event.ON_DESTROY) {
            if (currentLifecycleOwnerId == ownerId) {
                currentLifecycleOwnerId = null
                timestamp = Timestamp(0)
                Simber.i("Lifecycle owner [$ownerId] removed from main executor", tag = ORCHESTRATION)
            }
        }
    }

    /**
     * Check for whether enough time has passed since the last set of the [currentLifecycleOwnerId].
     *
     * @return true when the difference between current time and the [timestamp] is greater than
     * amount of seconds specified in the [executionTimeLimitSec]. False otherwise.
     */
    private fun enoughTimePassedSinceLastLock() = timeHelper.msBetweenNowAndTime(timestamp) > 1000 * executionTimeLimitSec

    fun LifecycleOwner.id(): Int = hashCode()

    /**
     * Checks whether the current activity (more technically, its [LifecycleOwner]) has rights to be
     * executed at this moment.
     *
     * @param activity reference to the corresponding [LifecycleOwner]
     * @return true if there are no other instances of this lifecycle owner running. False
     * otherwise.
     */
    fun isMain(activity: LifecycleOwner): Boolean = currentLifecycleOwnerId == activity.hashCode()
}
