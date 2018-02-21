package com.simprints.id.tools.base

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before

open class RxJavaTest {

    @Before
    fun setupClass() {

        // AndroidSchedulers.mainThread() requests Android APi, not available in unit tests
        // Setting a special main Scheduler https://medium.com/@peter.tackage/overriding-rxandroid-schedulers-in-rxjava-2-5561b3d14212
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { _ -> Schedulers.trampoline() }
    }
}
