package com.simprints.id.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
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
import com.simprints.id.tools.base.RxJavaTest
import com.simprints.id.tools.roboletric.*
import junit.framework.Assert.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class CheckLoginFromIntentActivityTest : RxJavaTest() {

    companion object {
        const val DEFAULT_ACTION = "com.simprints.id.REGISTER"
        const val DEFAULT_PROJECT_ID = "some_project_id"
        const val DEFAULT_PROJECT_SECRET = "some_project_secret"
        const val DEFAULT_USER_ID = "some_user_id"
    }

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        app = RuntimeEnvironment.application as TestApplication
        sharedPreferences = getRoboSharedPreferences()

        mockLocalDbManager(app)
        mockRemoteDbManager(app)
        mockIsSignedIn(app, sharedPreferences)
        mockDbManager(app)
        mockAnalyticsMock()
    }

    private fun mockAnalyticsMock() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
    }

    @Test
    fun unknownCallingAppSource_shouldLogEvent() {
        val controller = Robolectric.buildActivity(CheckLoginFromIntentActivity::class.java).create()
        val spyAct = spy(controller.get())
        doReturn("com.app.installed.manually").`when`(spyAct).getCallingPackageName()
        replaceActivityInController(controller, spyAct)

        controller.start().resume().visible()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    fun knownCallingAppSource_shouldNotLogEvent() {
        val pm = app.packageManager
        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")

        val controller = Robolectric.buildActivity(CheckLoginFromIntentActivity::class.java)

        val spyAct = spy(controller.get())
        doReturn("com.app.installed.from.playstore").`when`(spyAct).getCallingPackageName()
        replaceActivityInController(controller, spyAct)
        controller.create().start().resume().visible()
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

        val activity = startCheckLoginActivity(true)
        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    fun userIsNotLogged_shouldLoginActComeUp() {

        val activity = startCheckLoginActivity(false)
        assertActivityStarted(LoginActivity::class.java, activity)
    }

    @Test
    fun normalFlowReturnsAResult_shouldForwardItBackToCallingApp() {
        val activity = startCheckLoginActivity(true)
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
        val controller = createCheckLoginController(false)
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)

        setUserLogInState(true)

        sActivity.receiveResult(
            intent,
            LoginActivity.LOGIN_REQUEST_CODE,
            Intent())

        controller.resume()
        assertActivityStarted(LaunchActivity::class.java, sActivity)
    }

    @Test
    fun loginFailed_shouldCloseApp() {
        val controller = createCheckLoginController(false)
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

    // if logged is true, it sets the login credentials in the shared prefs and creates an intent
    // with the same values to start the login activity.
    private fun startCheckLoginActivity(logged: Boolean,
                                        actionString: String = DEFAULT_ACTION,
                                        projectId: String = DEFAULT_PROJECT_ID,
                                        userId: String = DEFAULT_USER_ID): CheckLoginFromIntentActivity {

        val controller = createCheckLoginController(logged, actionString, projectId, userId)
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        return activity
    }

    private fun createCheckLoginController(logged: Boolean,
                                           actionString: String = DEFAULT_ACTION,
                                           projectId: String = DEFAULT_PROJECT_ID,
                                           userId: String = DEFAULT_USER_ID): ActivityController<CheckLoginFromIntentActivity> {

        setUserLogInState(logged, projectId, userId)
        val intent = createACallingAppIntent(actionString, projectId, userId)
        return createRoboCheckLoginFromIntentViewActivity(intent).start()
    }

    private fun setUserLogInState(logged: Boolean,
                                  projectId: String = DEFAULT_PROJECT_ID,
                                  userId: String = DEFAULT_USER_ID,
                                  projectSecret: String = DEFAULT_PROJECT_SECRET) {

        val editor = sharedPreferences.edit()
        editor.putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, if (logged) projectSecret else "").apply()
        editor.putString(SecureDataManagerImpl.PROJECT_ID, if (logged) projectId else "").apply()
        editor.putString(SecureDataManagerImpl.USER_ID, if (logged) userId else "").apply()
        editor.putBoolean("IS_FIREBASE_TOKEN_VALID", logged).apply()
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
