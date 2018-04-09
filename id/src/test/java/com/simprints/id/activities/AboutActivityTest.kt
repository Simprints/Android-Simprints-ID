package com.simprints.id.activities

import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.createRoboAboutActivity
import com.simprints.id.testUtils.roboletric.mockDbManager
import kotlinx.android.synthetic.main.activity_about.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class AboutActivityTest {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        app = (RuntimeEnvironment.application as Application)
        mockDbManager(app)
        mockAnalyticsManager()
    }

    private fun mockAnalyticsManager() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
    }

    @Test
    fun versionTextViews_shouldBePopulated() {
        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.tv_appVersion.text.isNotEmpty())
        assertTrue(activity.tv_libsimprintsVersion.text.isNotEmpty())
        assertTrue(activity.tv_scannerVersion.text.isNotEmpty())
    }

    @Test
    fun countTextViews_shouldBePopulated() {
        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertEquals("0", activity.tv_userDbCount.text)
        assertEquals("0", activity.tv_moduleDbCount.text)
        assertEquals("0", activity.tv_globalDbCount.text)
    }

    @Test
    fun recoverDbButton_disablesOncePressed() {
        val controller = createRoboAboutActivity().start().resume().visible()
        val activity = controller.get()

        assertTrue(activity.bt_recoverDb.isEnabled)

        activity.bt_recoverDb.performClick()

        assertFalse(activity.bt_recoverDb.isEnabled)
    }
}
