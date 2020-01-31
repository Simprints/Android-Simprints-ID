package com.simprints.testtools.unit

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
open class BaseUnitTestConfig {

    private val schedulerInstance = Schedulers.trampoline()

    open fun rescheduleRxMainThread() = also {
        // AndroidSchedulers.mainThread() requests Android APi, not available in unit tests
        // Setting a special main Scheduler https://medium.com/@peter.tackage/overriding-rxandroid-schedulers-in-rxjava-2-5561b3d14212
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { schedulerInstance }
        RxJavaPlugins.setIoSchedulerHandler { schedulerInstance }
        RxJavaPlugins.setNewThreadSchedulerHandler { schedulerInstance }
        RxJavaPlugins.setComputationSchedulerHandler { schedulerInstance }
    }

    open fun coroutinesMainThread() = apply {
        Dispatchers.setMain(TestCoroutineDispatcher())
    }

}
