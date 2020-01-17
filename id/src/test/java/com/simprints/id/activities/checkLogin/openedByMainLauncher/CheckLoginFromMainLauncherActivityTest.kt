package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.app.Activity
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.data.db.common.RemoteDbManager
import com.simprints.id.data.loginInfo.LoginInfoManagerImpl
import com.simprints.id.data.prefs.PreferencesManagerImpl
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.state.RobolectricTestMocker.SHARED_PREFS_FOR_MOCK_FIREBASE_TOKEN_VALID
import com.simprints.id.testtools.state.RobolectricTestMocker.initLogInStateMock
import com.simprints.id.testtools.state.RobolectricTestMocker.setUserLogInState
import com.simprints.testtools.common.di.DependencyRule.MockRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import com.simprints.testtools.unit.robolectric.createActivity
import com.simprints.testtools.unit.robolectric.getSharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CheckLoginFromMainLauncherActivityTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    private lateinit var editor: SharedPreferences.Editor

    @Inject lateinit var remoteDbManagerMock: RemoteDbManager

    private val module by lazy {
        TestAppModule(app,
            remoteDbManagerRule = MockRule,
            secureDataManagerRule = MockRule,
            deviceManagerRule = MockRule)
    }

    @Before
    fun setUp() {
        UnitTestConfig(this, module).fullSetup()

        val sharedPrefs = getSharedPreferences(PreferencesManagerImpl.PREF_FILE_NAME)
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
        val controller = createActivity<CheckLoginFromMainLauncherActivity>()
        val activity = controller.get()
        controller.resume().visible()
        assertActivityStarted(clazzNextActivity, activity)
    }
}
