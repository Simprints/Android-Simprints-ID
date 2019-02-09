package com.simprints.testframework.unit.reactive

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers

fun rescheduleRxMainThread() {
    // AndroidSchedulers.mainThread() requests Android APi, not available in unit tests
    // Setting a special main Scheduler https://medium.com/@peter.tackage/overriding-rxandroid-schedulers-in-rxjava-2-5561b3d14212
    RxAndroidPlugins.setInitMainThreadSchedulerHandler { SCHEDULER_INSTANCE }
    RxJavaPlugins.setIoSchedulerHandler { SCHEDULER_INSTANCE }
    RxJavaPlugins.setNewThreadSchedulerHandler { SCHEDULER_INSTANCE }
    RxJavaPlugins.setComputationSchedulerHandler { SCHEDULER_INSTANCE }
}

val SCHEDULER_INSTANCE = Schedulers.trampoline()
