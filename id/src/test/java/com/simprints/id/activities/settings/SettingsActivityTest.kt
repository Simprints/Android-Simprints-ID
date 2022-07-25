package com.simprints.id.activities.settings

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.Application
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.createActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SettingsActivityTest {

    private val app = ApplicationProvider.getApplicationContext<Application>()

    private val module by lazy {
        TestAppModule(
            app,
            dbManagerRule = DependencyRule.MockkRule,
            sessionEventsLocalDbManagerRule = DependencyRule.MockkRule
        )
    }

    @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        UnitTestConfig(module).fullSetup().inject(this)
    }

    @Test
    fun `check activity finishes when correct result is sent`() {

        val controller = createRoboActivity()
        val activity = controller.get()

        activity.openSettingAboutActivity()

        val intent = shadowOf(activity).nextStartedActivity

        shadowOf(activity).receiveResult(intent, 1, Intent())

        assert(activity.isFinishing)
        assert(shadowOf(activity).resultCode == 1)
    }

    @Test
    fun `check activity doesn't finish when result is cancelled`() {

        val controller = createRoboActivity()
        val activity = controller.get()

        activity.openSettingAboutActivity()

        val intent = shadowOf(activity).nextStartedActivity

        shadowOf(activity).receiveResult(intent, Activity.RESULT_CANCELED, Intent())

        assert(!activity.isFinishing)
    }

    private fun createRoboActivity() = createActivity<SettingsActivity>()

}
