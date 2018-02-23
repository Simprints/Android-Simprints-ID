package com.simprints.id.activities

import android.app.Activity
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.roboletric.TestApplication
import com.simprints.id.tools.roboletric.createRoboCheckLoginMainLauncherAppActivity
import com.simprints.id.tools.roboletric.getRoboSharedPreferences
import com.simprints.id.tools.roboletric.mockCheckFirebaseTokenMock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, application = TestApplication::class)
class CheckLoginFromMainLauncherActivityTest {

    private lateinit var app: Application
    private lateinit var sharedPrefs: SharedPreferences
    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)

        sharedPrefs = getRoboSharedPreferences()
        mockCheckFirebaseTokenMock(app, sharedPrefs)

        sharedPrefs.edit().putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, "encrypted_project_secret").commit()
        sharedPrefs.edit().putString(SecureDataManagerImpl.PROJECT_ID, "projectId").commit()
        sharedPrefs.edit().putBoolean("IS_FIREBASE_TOKEN_VALID", true).commit()

        app.dataManager.initialiseDb()
    }

    @Test
    fun appNotSignedInFirebase_shouldRequestLoginActComeUp() {
        sharedPrefs.edit().putBoolean("IS_FIREBASE_TOKEN_VALID", false).commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun projectIdEmpty_shouldRequestLoginActComeUp() {
        getRoboSharedPreferences().edit().putString(SecureDataManagerImpl.PROJECT_ID, "").commit()
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun projectSecretEmpty_shouldRequestLoginActComeUp() {
        getRoboSharedPreferences().edit().putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, "").commit()
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
