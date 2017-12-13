package com.simprints.id.tools

import android.os.SystemClock
import android.support.test.espresso.IdlingPolicies.setMasterPolicyTimeout
import com.schibsted.spain.barista.BaristaSleepActions.sleep
import java.util.concurrent.TimeUnit


object WaitingUtils {

    fun waitOnUiForSetupToFinish() {
        sleep(12, TimeUnit.SECONDS)
    }

    fun waitOnUiForScanningToComplete() {
        sleep(8, TimeUnit.SECONDS)
    }

    fun waitOnUiForActivityToSettle() {
        sleep(2, TimeUnit.SECONDS)
    }

    fun waitOnUiForMatchingToComplete() {
        sleep(8, TimeUnit.SECONDS)
    }

    fun waitOnUiForMediumSyncInterval() {
        setMasterPolicyTimeout(SyncParameters.MEDIUM_DATABASE_SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        sleep(SyncParameters.SYNC_CHECK_INTERVAL, TimeUnit.SECONDS)
    }

    fun waitOnSystemForQuickDataCalls() {
        SystemClock.sleep(2000)
    }

    fun waitOnSystemToSettle() {
        SystemClock.sleep(2000)
    }

    fun waitOnUiForSyncingToStart() {
        sleep(1, TimeUnit.SECONDS)
    }

}
