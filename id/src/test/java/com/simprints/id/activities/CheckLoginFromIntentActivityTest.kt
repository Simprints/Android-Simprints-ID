package com.simprints.id.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity
import com.simprints.id.activities.checkLogin.openedByIntent.CheckLoginFromIntentActivity.Companion.LOGIN_REQUEST_CODE
import com.simprints.id.activities.launch.LaunchActivity
import com.simprints.id.activities.login.LoginActivity
import com.simprints.id.data.analytics.AnalyticsManager
import com.simprints.id.data.analytics.FirebaseAnalyticsManager
import com.simprints.id.shared.anyNotNull
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.testUtils.base.RxJavaTest
import com.simprints.id.testUtils.roboletric.*
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    application = TestApplication::class,
    sdk = [Build.VERSION_CODES.N_MR1])
// for O_MR1 = 27, Roboletric hangs around after it calls SharedPrefs.'apply' and then it accesses to
// the SharedPrefs again. https://github.com/robolectric/robolectric/issues/3641
class CheckLoginFromIntentActivityTest : RxJavaTest() {

    companion object {
        const val DEFAULT_ACTION = "com.simprints.id.REGISTER"
        const val DEFAULT_PROJECT_ID = "some_project_id"
        const val DEFAULT_PROJECT_SECRET = "some_project_secret"
        const val DEFAULT_REALM_KEY = "enc_some_key"
        const val DEFAULT_USER_ID = "some_user_id"
        const val DEFAULT_MODULE_ID = "some_module_id"
        const val DEFAULT_LEGACY_API_KEY = "96307ff9-873b-45e0-8ef0-b2efd5bef12d"
    }

    private lateinit var analyticsManagerMock: AnalyticsManager
    private lateinit var app: Application
    private lateinit var sharedPrefs: SharedPreferences

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)

        sharedPrefs = getRoboSharedPreferences()

        createMockForLocalDbManager(app)
        createMockForRemoteDbManager(app)
        createMockForSecureDataManager(app)

        initLogInStateMock(app, sharedPrefs)
        mockAnalyticsManager()

        createMockForDbManager(app)
        app.dbManager.initialiseDb()
    }

    private fun mockAnalyticsManager() {
        analyticsManagerMock = Mockito.mock(FirebaseAnalyticsManager::class.java)
        app.analyticsManager = analyticsManagerMock
    }

    @Test
    fun unknownCallingAppSource_shouldLogEvent() {
        Robolectric.buildActivity(CheckLoginFromIntentActivityWithInvalidCallingPackage::class.java).setup()
        verifyALogSafeExceptionWasThrown(1)
    }

    @Test
    fun knownCallingAppSource_shouldNotLogEvent() {
        val pm = app.packageManager
        pm.setInstallerPackageName("com.app.installed.from.playstore", "com.android.vending")

        Robolectric.buildActivity(CheckLoginFromIntentActivityWithValidCallingPackage::class.java).setup()
        verifyALogSafeExceptionWasThrown(0)
    }

    private fun verifyALogSafeExceptionWasThrown(times: Int) {
        Mockito.verify(analyticsManagerMock, Mockito.times(times)).logSafeException(anyNotNull())
    }

    @Test
    fun validIntentParams_extractParamsForLogin() {

        val intent = createACallingAppIntentWithProjectId()

        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
        controller.resume().visible()

        Assert.assertEquals(DEFAULT_PROJECT_ID, app.dataManager.projectId)
        Assert.assertEquals(DEFAULT_USER_ID, app.dataManager.userId)
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

        setUserLogInState(true, sharedPrefs)
        val checkLoginIntent = createACallingAppIntentWithLegacyApiKey()
        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)

        assertActivityStarted(LaunchActivity::class.java, activity)
    }

    @Test
    fun userIsNotLogged_shouldLoginActComeUp() {

        setUserLogInState(false, sharedPrefs)
        val checkLoginIntent = createACallingAppIntentWithProjectId()
        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)

        assertActivityStarted(LoginActivity::class.java, activity)
    }

    @Test
    fun userIsNotLoggedAndCallingAppUsesApiLegacyKey_shouldLoginActWithLegacyApiKeyComeUp() {

        setUserLogInState(false, sharedPrefs)
        val checkLoginIntent = createACallingAppIntentWithLegacyApiKey()
        val activity = startCheckLoginFromIntentActivity(checkLoginIntent)
        val sActivity = shadowOf(activity)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)
        assertEquals(DEFAULT_LEGACY_API_KEY, intent.getStringExtra(IntentKeys.loginActivityLegacyProjectIdKey))
    }

    @Test
    fun normalFlowReturnsAResult_shouldForwardItBackToCallingApp() {
        setUserLogInState(true, sharedPrefs)
        val loginIntent = createACallingAppIntentWithProjectId()
        val activity = startCheckLoginFromIntentActivity(loginIntent)
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
        setUserLogInState(false, sharedPrefs)
        val loginIntent = createACallingAppIntentWithProjectId()
        val controller = createRoboCheckLoginFromIntentViewActivity(loginIntent).start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)

        setUserLogInState(true, sharedPrefs)

        sActivity.receiveResult(
            intent,
            LOGIN_REQUEST_CODE,
            Intent())

        controller.resume()
        assertActivityStarted(LaunchActivity::class.java, sActivity)
        assertFalse(sActivity.isFinishing)
    }

    @Test
    fun loginFailed_shouldCloseApp() {
        setUserLogInState(false, sharedPrefs)
        val loginIntent = createACallingAppIntentWithProjectId()
        val controller = createRoboCheckLoginFromIntentViewActivity(loginIntent).start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        val sActivity = shadowOf(activity)

        assertFalse(sActivity.isFinishing)
        val intent = sActivity.nextStartedActivity
        assertActivityStarted(LoginActivity::class.java, intent)

        sActivity.receiveResult(
            intent,
            LOGIN_REQUEST_CODE,
            Intent())

        controller.resume()
        assertTrue(activity.isFinishing)
    }

    private fun startCheckLoginFromIntentActivity(intent: Intent): CheckLoginFromIntentActivity {

        val controller = createRoboCheckLoginFromIntentViewActivity(intent).start()
        val activity = controller.get() as CheckLoginFromIntentActivity
        controller.resume().visible()
        return activity
    }

    private fun createACallingAppIntentWithLegacyApiKey(actionString: String = DEFAULT_ACTION,
                                                        legacyApiKey: String = DEFAULT_LEGACY_API_KEY,
                                                        userId: String = DEFAULT_USER_ID,
                                                        moduleId: String = DEFAULT_MODULE_ID): Intent {

        return createACallingAppIntent(actionString, userId, moduleId).also { it.putExtra("apiKey", legacyApiKey) }
    }

    private fun createACallingAppIntentWithProjectId(actionString: String = DEFAULT_ACTION,
                                                     projectId: String = DEFAULT_PROJECT_ID,
                                                     userId: String = DEFAULT_USER_ID,
                                                     moduleId: String = DEFAULT_MODULE_ID): Intent {

        return createACallingAppIntent(actionString, userId, moduleId).also { it.putExtra("projectId", projectId) }
    }

    private fun createACallingAppIntent(actionString: String,
                                        userId: String = "",
                                        moduleId: String = ""): Intent {

        return Intent().apply {
            action = actionString
            putExtra("userId", userId)
            putExtra("moduleId", moduleId)
        }
    }
}

class CheckLoginFromIntentActivityWithInvalidCallingPackage : CheckLoginFromIntentActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.manually"
    }
}

class CheckLoginFromIntentActivityWithValidCallingPackage : CheckLoginFromIntentActivity() {

    override fun getCallingPackageName(): String {
        return "com.app.installed.from.playstore"
    }
}
