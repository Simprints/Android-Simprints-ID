package com.simprints.id.testUtils.base

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

interface RxJavaTest {

    @Before
    fun setupClass() {

        // AndroidSchedulers.mainThread() requests Android APi, not available in unit tests
        // Setting a special main Scheduler https://medium.com/@peter.tackage/overriding-rxandroid-schedulers-in-rxjava-2-5561b3d14212
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { RxSchedulerRule.SCHEDULER_INSTANCE }
        RxJavaPlugins.setIoSchedulerHandler { RxSchedulerRule.SCHEDULER_INSTANCE }
        RxJavaPlugins.setNewThreadSchedulerHandler { RxSchedulerRule.SCHEDULER_INSTANCE }
        RxJavaPlugins.setComputationSchedulerHandler { RxSchedulerRule.SCHEDULER_INSTANCE }
    }
}


//Same as RxJavaTest, but it can be used as @get:Rule val rxSchedulerRule = RxSchedulerRule()
class RxSchedulerRule : TestRule {
    override fun apply(base: Statement, description: Description) =
        object : Statement() {
            override fun evaluate() {
                RxAndroidPlugins.reset()
                RxAndroidPlugins.setInitMainThreadSchedulerHandler { SCHEDULER_INSTANCE }

                RxJavaPlugins.reset()
                RxJavaPlugins.setIoSchedulerHandler { SCHEDULER_INSTANCE }
                RxJavaPlugins.setNewThreadSchedulerHandler { SCHEDULER_INSTANCE }
                RxJavaPlugins.setComputationSchedulerHandler { SCHEDULER_INSTANCE }

                base.evaluate()
            }
        }

    companion object {
        val SCHEDULER_INSTANCE = Schedulers.trampoline()
    }
}
