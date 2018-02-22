package com.simprints.id.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import com.simprints.id.tools.roboletric.mockRemoteDbManager
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class CheckLoginFromIntentActivityTest {

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        app = Application()
        mockDbManagers(app)
        RuntimeEnvironment.application = app
        sharedPreferences = getRoboSharedPreferences()
    }

    @Test
    fun unknownCallingAppSource_shouldLogEvent() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock

        val controller = Robolectric.buildActivity(CheckLoginActivityFromBadCallingMock::class.java).create()
        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    fun knownCallingAppSource_shouldNotLogEvent() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
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
    fun validIntentParams_extractParamsForLogin() {

        val intent = createACallingAppIntent("com.simprints.id.REGISTER", "some_projectId", "some_userId")

        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
        controller.resume().visible()

        Assert.assertEquals("some_projectId", app.dataManager.projectId)
        Assert.assertEquals("some_userId", app.dataManager.userId)
    }

    @Test
    fun invalidParams_shouldAlertActComeUp() {

        val controller = createRoboCheckLoginFromIntentViewActivity().start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.visible()

        assertActivityStarted(AlertActivity::class.java, activity)
    }

    @Test
    fun userIsLogged_shouldLaunchActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", true)
        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    fun userIsNotLogged_shouldLoginActComeUp() {

        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", false)
        assertActivityStarted(LoginActivity::class.java, activity)
    }

    @Test
    fun normalFlowReturnsAResult_shouldForwardItBackToCallingApp() {
        val activity = startCheckLoginActivity("com.simprints.id.REGISTER", "some_projectId", true)
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LaunchActivity::class.java, intent)

        sActivity.receiveResult(
            intent,
            Activity.RESULT_OK,
            Intent().putExtra("result", "some_result"))

        assertTrue(sActivity.isFinishing)
        assertEquals(sActivity.resultCode, Activity.RESULT_OK)
        assertEquals(sActivity.resultIntent.getStringExtra("result"), "some_result")
    }

    @Test
    fun loginSucceed_shouldOpenLaunchActivity() {
        val controller = createCheckLoginController("com.simprints.id.REGISTER", "some_projectId", false)
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)

        setUserLogInStateForProjectId("some_projectId", true)

        sActivity.receiveResult(
            intent,
            LoginActivity.LOGIN_REQUEST_CODE,
            Intent())

        controller.resume()
        assertFalse(activity.isFinishing)
        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    fun loginFailed_shouldCloseApp() {
        val controller = createCheckLoginController("com.simprints.id.REGISTER", "some_projectId", false)
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)

        sActivity.receiveResult(
            intent,
            LoginActivity.LOGIN_REQUEST_CODE,
            Intent())

        controller.resume()
        assertTrue(activity.isFinishing)
    }

    private fun startCheckLoginActivity(actionString: String, projectId: String, logged: Boolean): CheckLoginFromIntentActivity {
        val controller = createCheckLoginController(actionString, projectId, logged)
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        return activity
    }

    private fun createCheckLoginController(actionString: String, projectId: String, logged: Boolean): ActivityController<CheckLoginFromIntentActivity> {
        setUserLogInStateForProjectId(projectId, logged)
        val intent = createACallingAppIntent(actionString, projectId)
        return createRoboCheckLoginFromIntentViewActivity(intent).start()
    }

    private fun setUserLogInStateForProjectId(projectId: String, logged: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, if (logged) "some_secret" else "")
        editor.putString(SecureDataManagerImpl.PROJECT_ID, if (logged) projectId else "$projectId false")
        editor.apply()
        Mockito.reset(app.remoteDbManager)
        mockRemoteDbManager(app)
        doReturn(logged).`when`(app.remoteDbManager).isSignedIn(anyNotNull(), anyNotNull())
    }

    private fun createACallingAppIntent(actionString: String, projectId: String = "", userId: String = "", moduleId: String = ""): Intent {
        return Intent().apply {
            action = actionString
            putExtra("projectId", projectId)
            putExtra("userId", userId)
            putExtra("moduleId", moduleId)
        }
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
