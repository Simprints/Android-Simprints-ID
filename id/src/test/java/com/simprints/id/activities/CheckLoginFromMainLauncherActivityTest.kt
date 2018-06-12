package com.simprints.id.activities

import android.app.Activity
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.DataManager
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.di.AppModuleForTests
import com.simprints.id.di.DaggerForTests
import com.simprints.id.shared.MockRule.*
import com.simprints.id.testUtils.assertActivityStarted
import com.simprints.id.testUtils.roboletric.*
import com.simprints.id.tools.delegates.lazyVar
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(RobolectricTestRunner::class)
@Config(application = TestApplication::class)
class CheckLoginFromMainLauncherActivityTest : DaggerForTests() {

    private lateinit var editor: SharedPreferences.Editor

    @Inject
    lateinit var remoteDbManagerMock: RemoteDbManager

    @Inject
    lateinit var dbManager: DbManager

    @Inject
    lateinit var dataManager: DataManager

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = MOCK,
            remoteDbManagerRule = MOCK,
            secureDataManagerRule = MOCK)
    }

    @Before
    override fun setUp() {
        FirebaseApp.initializeApp(RuntimeEnvironment.application)
        app = (RuntimeEnvironment.application as TestApplication)
        super.setUp()
        testAppComponent.inject(this)
        dbManager.initialiseDb()

        val sharedPrefs = getRoboSharedPreferences()
        editor = sharedPrefs.edit()

        initLogInStateMock(sharedPrefs, remoteDbManagerMock)
        setUserLogInState(true, sharedPrefs)
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
