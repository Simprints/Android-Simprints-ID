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

    fun waitOnUiForMediumSyncToComplete() {
        setMasterPolicyTimeout(120, TimeUnit.SECONDS)
        sleep(60, TimeUnit.SECONDS)
    }

    fun waitOnSystemForQuickDataCalls() {
        SystemClock.sleep(2000)
    }

    fun waitOnSystemToSettle() {
        SystemClock.sleep(2000)
    }
}
