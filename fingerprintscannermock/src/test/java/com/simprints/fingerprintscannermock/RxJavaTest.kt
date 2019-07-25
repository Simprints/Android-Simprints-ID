package com.simprints.fingerprintscannermock

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before

interface RxJavaTest {

    @Before
    fun setupClass() {

        // AndroidSchedulers.mainThread() requests Android APi, not available in unit tests
        // Setting a special main Scheduler https://medium.com/@peter.tackage/overriding-rxandroid-schedulers-in-rxjava-2-5561b3d14212
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { _ -> Schedulers.trampoline() }
        RxJavaPlugins.setIoSchedulerHandler { _ -> Schedulers.trampoline() }
    }
}
