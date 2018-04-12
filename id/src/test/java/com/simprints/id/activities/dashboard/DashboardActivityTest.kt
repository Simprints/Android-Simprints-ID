package com.simprints.id.activities.dashboard

import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.testUtils.roboletric.mockDbManager
import com.simprints.id.testUtils.roboletric.mockLocalDbManager
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class DashboardActivityTest {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        app = (RuntimeEnvironment.application as Application)
        mockLocalDbManager(app)
        mockDbManager(app)
        mockAnalyticsManager()
    }

    private fun mockAnalyticsManager() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
    }
}
