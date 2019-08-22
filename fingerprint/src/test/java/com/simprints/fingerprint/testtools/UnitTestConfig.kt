package com.simprints.fingerprint.testtools

import com.simprints.testtools.unit.BaseUnitTestConfig

class UnitTestConfig() : BaseUnitTestConfig() {

    fun fullSetup() =
        rescheduleRxMainThread()
            .coroutinesMainThread()

    override fun rescheduleRxMainThread() = also {
        super.rescheduleRxMainThread()
    }

    override fun coroutinesMainThread() = also {
        super.coroutinesMainThread()
    }
}
