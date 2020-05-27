package com.simprints.testtools.common.reactive

import io.reactivex.schedulers.TestScheduler
import java.util.concurrent.TimeUnit

fun TestScheduler.advanceTime(seconds: Int = 60) {
    advanceTimeBy(seconds.toLong(), TimeUnit.SECONDS)
}
