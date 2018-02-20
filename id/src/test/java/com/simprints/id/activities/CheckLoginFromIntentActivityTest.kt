package com.simprints.id.activities

import android.content.Intent
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.roboletric.createRoboCheckLoginFromIntentViewActivity
import com.simprints.id.tools.roboletric.getRoboSharedPreferences
import com.simprints.id.tools.roboletric.mockDbManagers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class CheckLoginFromIntentActivityTest {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        analyticsManagerMock = mock(FirebaseAnalyticsManager::class.java)
        app = (RuntimeEnvironment.application as Application)
        app.analyticsManager = analyticsManagerMock

        mockDbManagers(app)
    }

    @Test
    @Throws(Exception::class)
    fun unknownCallingAppSource_shouldLogEvent() {
        val controller = Robolectric.buildActivity(CheckLoginActivityFromBadCallingMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    @Throws(Exception::class)
    fun knownCallingAppSource_shouldNotLogEvent() {
        val pm = app.packageManager
        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")

        val controller = Robolectric.buildActivity(CheckLoginActivityFromGoodCallingAppMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(0)
    }

    private fun verifyALogSafeExceptionWasThrown(times: Int) {
        Mockito.verify(analyticsManagerMock, Mockito.times(times)).logSafeException(anyNotNull())
    }

    @Test
    @Throws(Exception::class)
    fun validIntentParams_extractLoginOnes() {

        val intent = Intent().apply {
            action = "com.simprints.id.REGISTER"
            putExtra("projectId", "some_project")
            putExtra("apiKey", "some_apiKey")
            putExtra("userId", "some_userId")
            putExtra("moduleId", "some_module")
        }

        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
        controller.resume().visible()

        Assert.assertEquals("some_project", app.dataManager.projectId)
        Assert.assertEquals("some_userId", app.dataManager.userId)
    }

    @Test
    @Throws(Exception::class)
    fun invalidParams_shouldAlertActComeUp() {

        val controller = createRoboCheckLoginFromIntentViewActivity().start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()

        assertActivityStarted(AlertActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun userIsLogged_shouldLaunchActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", true)
        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    @Throws(Exception::class)
    fun userIsNotLogged_shouldLoginActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", false)
        assertActivityStarted(LoginActivity::class.java, activity)
    }

    private fun startCheckLoginActivity(actionString: String, projectId: String, logged: Boolean): CheckLoginFromIntentActivity {
        val sharedPreferences = getRoboSharedPreferences()
        sharedPreferences.edit().putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, if (logged) "some_secret" else "").commit()
        sharedPreferences.edit().putString(SecureDataManagerImpl.PROJECT_ID, if (logged) projectId else "$projectId false").commit()
        doReturn(logged).`when`(app.remoteDbManager).isSignedIn(anyNotNull())

        val intent = Intent().apply {
            action = actionString
            putExtra("projectId", projectId)
            putExtra("userId", "")
            putExtra("moduleId", "")
        }

        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        return activity
    }
}

class CheckLoginActivityFromBadCallingMock : CheckLoginFromIntentActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.manually"
    }
}

class CheckLoginActivityFromGoodCallingAppMock : CheckLoginFromIntentActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.from.playstore" //Any available app.
    }
}
