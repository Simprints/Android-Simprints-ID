package com.simprints.id.activities

import android.app.Activity
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.testUtils.roboletric.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CheckLoginFromMainLauncherActivityTest {

    private lateinit var app: Application
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)

        val sharedPrefs = getRoboSharedPreferences()
        editor = sharedPrefs.edit()

        createMockForLocalDbManager(app)
        createMockForRemoteDbManager(app)
        createMockForSecureDataManager(app)

        initLogInStateMock(app, sharedPrefs)
        setUserLogInState(true, sharedPrefs)

        createMockForDbManager(app)
        app.dbManager.initialiseDb()
    }

    @Test
    fun appNotSignedInFirebase_shouldRequestLoginActComeUp() {
        editor.putBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, false).commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun projectIdEmpty_shouldRequestLoginActComeUp() {
        editor.putString(LoginInfoManagerImpl.PROJECT_ID, "").commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun projectSecretEmpty_shouldRequestLoginActComeUp() {
        editor.putString(LoginInfoManagerImpl.ENCRYPTED_PROJECT_SECRET, "").commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun userIdEmpty_shouldRequestLoginActComeUp() {
        editor.putString(LoginInfoManagerImpl.USER_ID, "").commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun localDbNotValid_shouldRequestLoginActComeUp() {
        editor.putBoolean(SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID, false).commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun userIsLogged_shouldDashboardActComeUp() {
        startCheckLoginAndCheckNextActivity(DashboardActivity::class.java)
    }

    private fun startCheckLoginAndCheckNextActivity(clazzNextActivity: Class<out Activity>) {
        val controller = createRoboCheckLoginMainLauncherAppActivity()
        val activity = controller.get()
        controller.resume().visible()
        assertActivityStarted(clazzNextActivity, activity)
    }
}
