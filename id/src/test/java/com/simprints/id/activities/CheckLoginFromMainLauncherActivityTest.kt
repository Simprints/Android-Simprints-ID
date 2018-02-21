package com.simprints.id.activities

import android.app.Activity
import com.google.firebase.FirebaseApp
import com.simprints.id.Application
import com.simprints.id.BuildConfig
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.secure.SecureDataManagerImpl
import com.simprints.id.testUtils.anyNotNull
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.tools.roboletric.createRoboCheckLoginMainLauncherAppActivity
import com.simprints.id.tools.roboletric.getRoboSharedPreferences
import com.simprints.id.tools.roboletric.mockDbManagers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class)
class CheckLoginFromMainLauncherActivityTest {

    private lateinit var app: Application

    @Before
    fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as Application)

        mockDbManagers(app)
        val sharedPreferences = getRoboSharedPreferences()
        sharedPreferences.edit().putString(SecureDataManagerImpl.ENCRYPTED_PROJECT_SECRET, "encrypted_project_secret").commit()
        sharedPreferences.edit().putString(SecureDataManagerImpl.PROJECT_ID, "projectId").commit()
    }

    @Test
    fun appNotSignedInFirebase_shouldRequestLoginActComeUp() {
        doReturn(false).`when`(app.remoteDbManager).isSignedIn(anyNotNull())
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
        doReturn(true).`when`(app.remoteDbManager).isSignedIn(anyNotNull())
        startCheckLoginAndCheckNextActivity(DashboardActivity::class.java)
    }

    private fun startCheckLoginAndCheckNextActivity(clazzNextActivity: Class<out Activity>) {
        val controller = createRoboCheckLoginMainLauncherAppActivity()
        val activity = controller.get()
        controller.resume().visible()
        assertActivityStarted(clazzNextActivity, activity)
    }
}
