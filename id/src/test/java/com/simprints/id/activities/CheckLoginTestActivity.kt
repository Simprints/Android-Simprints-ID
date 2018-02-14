package com.simprints.id.activities

import android.content.Intent
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.checkLogin.CheckLoginActivity
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.roboletric.createRoboCheckLoginViewActivity
import com.simprints.id.tools.roboletric.getRoboSharedPreferences
import com.simprints.id.tools.roboletric.mockLocalDbManager
import org.junit.Assert
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
class CheckLoginTestActivity {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        analyticsManagerMock = mock(FirebaseAnalyticsManager::class.java)
        app = (RuntimeEnvironment.application as Application)
        app.analyticsManager = analyticsManagerMock

        mockLocalDbManager(app)
    }

    @Test
    @Throws(Exception::class)
    fun unknownCallingAppSource_shouldLogEvent() {
        var controller = Robolectric.buildActivity(CheckLoginActivityFromBadCallingMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    @Throws(Exception::class)
    fun knownCallingAppSource_shouldNotLogEvent() {
        val pm = app.packageManager
        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")

        var controller = Robolectric.buildActivity(CheckLoginActivityFromGoodCallingAppMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(0)
    }

    private fun verifyALogSafeExceptionWasThrown(times: Int) {
        Mockito.verify(analyticsManagerMock, Mockito.times(times)).logSafeException(anyNotNull())
    }

    @Test
    @Throws(Exception::class)
    fun byIntent_extractLoginParams() {

        val intent = Intent().apply {
            action = "com.simprints.id.REGISTER"
            putExtra("projectId", "some_project")
            putExtra("apiKey", "some_apiKey")
            putExtra("userId", "some_userId")
            putExtra("moduleId", "some_module")
        }

        val controller = createRoboCheckLoginViewActivity(intent).start()
        val activity = controller.get() as CheckLoginActivity
        activity.viewPresenter.wasAppOpenedByIntent = true
        controller.resume().visible()

        Assert.assertEquals("some_project", app.dataManager.projectId)
        Assert.assertEquals("some_userId", app.dataManager.userId)
    }

    @Test
    @Throws(Exception::class)
    fun byIntent_invalidParams_shouldAlertActComeUp() {

        val controller = createRoboCheckLoginViewActivity().start()
        val activity = controller.get() as CheckLoginActivity
        activity.viewPresenter.wasAppOpenedByIntent = true
        controller.resume().visible()

        assertActivityStarted(AlertActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun byIntent_userIsLogged_shouldLaunchActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", true, true)
        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun byIntent_userIsNotLogged_shouldLoginActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", false, true)
        assertActivityStarted(LoginActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun byHomeButton_userIsNotLogged_shouldRequestLoginActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", false, false)
        assertActivityStarted(RequestLoginActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun byHomeButton_userIsLogged_shouldDashboardActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", true, false)
        assertActivityStarted(DashboardActivity::class.java, activity)
    }

    private fun startCheckLoginActivity(actionString: String, projectId: String, logged: Boolean, openedByIntent: Boolean): CheckLoginActivity {
        val sharedPreferences = getRoboSharedPreferences()
        sharedPreferences.edit().putString("ENCRYPTED_PROJECT_SECRET", if (logged) "some_secret" else "").commit()
        sharedPreferences.edit().putString("PROJECT_ID", if (logged) projectId else "$projectId false").commit()

        val intent = Intent().apply {
            action = actionString
            putExtra("projectId", projectId)
            putExtra("userId", "")
            putExtra("moduleId", "")
        }

        val controller = createRoboCheckLoginViewActivity(intent).start()
        val activity = controller.get() as CheckLoginActivity
        activity.viewPresenter.wasAppOpenedByIntent = openedByIntent
        controller.resume().visible()
        return activity
    }
}

class CheckLoginActivityFromBadCallingMock : CheckLoginActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.manually"
    }
}

class CheckLoginActivityFromGoodCallingAppMock : CheckLoginActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.from.playstore" //Any available app.
    }
}
