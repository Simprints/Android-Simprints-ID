package com.simprints.id.activities

import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.data.db.analytics.FirebaseAnalyticsManager
import com.simprints.id.testUtils.anyNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class LaunchActivityTest {

    private lateinit var analyticsManagerMock: FirebaseAnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        analyticsManagerMock = mock(FirebaseAnalyticsManager::class.java)
        app = (RuntimeEnvironment.application as Application)
        app.analyticsManager = analyticsManagerMock
    }

    @Test
    @Throws(Exception::class)
    fun unknownCallingAppSource_shouldLogEvent() {
        var controller = Robolectric.buildActivity(LaunchActivityFromBadCallingMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    @Throws(Exception::class)
    fun knownCallingAppSource_shouldNotLogEvent() {
        val pm = app.packageManager
        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")

        var controller = Robolectric.buildActivity(LaunchActivityFromGoodCallingAppMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(0)
    }

    private fun verifyALogSafeExceptionWasThrown(times: Int) {
        Mockito.verify(analyticsManagerMock, Mockito.times(times)).logSafeException(anyNotNull())
    }
}

class LaunchActivityFromBadCallingMock : LaunchActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.manually"
    }
}

class LaunchActivityFromGoodCallingAppMock : LaunchActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.from.playstore" //Any available app.
    }
}
