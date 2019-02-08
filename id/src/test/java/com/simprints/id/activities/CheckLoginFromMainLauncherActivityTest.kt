package com.simprints.id.activities

import android.app.Activity
import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherActivity
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.data.db.DbManager
import com.simprints.id.data.db.remote.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.shared.DependencyRule.MockRule
import com.simprints.id.testUtils.di.AppModuleForTests
import com.simprints.id.testUtils.di.DaggerForUnitTests
import com.simprints.id.testUtils.roboletric.RobolectricDaggerTestConfig
import com.simprints.id.testUtils.roboletric.RobolectricTestMocker.SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID
import com.simprints.id.testUtils.roboletric.RobolectricTestMocker.initLogInStateMock
import com.simprints.id.testUtils.roboletric.RobolectricTestMocker.setUserLogInState
import com.simprints.id.testUtils.roboletric.TestApplication
import com.simprints.id.tools.delegates.lazyVar
import com.simprints.testframework.unit.robolectric.RobolectricHelper
import com.simprints.testframework.unit.robolectric.RobolectricHelper.assertActivityStarted
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CheckLoginFromMainLauncherActivityTest : DaggerForUnitTests() {

    private lateinit var editor: SharedPreferences.Editor

    @Inject
    lateinit var remoteDbManagerMock: RemoteDbManager

    @Inject
    lateinit var dbManager: DbManager

    override var module by lazyVar {
        AppModuleForTests(app,
            localDbManagerRule = MockRule,
            remoteDbManagerRule = MockRule,
            secureDataManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        RobolectricDaggerTestConfig(this).setupAllAndFinish()
        dbManager.initialiseDb()

        val sharedPrefs = RobolectricHelper.getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME)
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
        val controller = RobolectricHelper.createActivity<CheckLoginFromMainLauncherActivity>()
        val activity = controller.get()
        controller.resume().visible()
        assertActivityStarted(clazzNextActivity, activity)
    }
}
