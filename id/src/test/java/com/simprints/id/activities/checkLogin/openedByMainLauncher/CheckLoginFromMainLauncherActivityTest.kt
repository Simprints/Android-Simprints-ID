package com.simprints.id.activities.checkLogin.openedByMainLauncher

import android.app.Activity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.dashboard.DashboardActivity
import com.simprints.id.activities.requestLogin.RequestLoginActivity
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.testtools.di.TestAppModule
import com.simprints.infra.login.db.RemoteDbManager
import com.simprints.infra.login.domain.LoginInfoManager
import com.simprints.testtools.common.di.DependencyRule.MockkRule
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import com.simprints.testtools.unit.robolectric.assertActivityStarted
import com.simprints.testtools.unit.robolectric.createActivity
import io.mockk.every
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class CheckLoginFromMainLauncherActivityTest {

    private val app = ApplicationProvider.getApplicationContext() as TestApplication

    @Inject
    lateinit var remoteDbManagerMock: RemoteDbManager

    @Inject
    lateinit var loginInfoManagerMock: LoginInfoManager

    private val module by lazy {
        TestAppModule(
            app,
            loginInfoManagerRule = MockkRule,
            remoteDbManagerRule = MockkRule,
            secureDataManagerRule = MockkRule
        )
    }

    @Before
    fun setUp() {
        UnitTestConfig(module).fullSetup().inject(this)
    }

    @Test
    fun appNotSignedInFirebase_shouldRequestLoginActComeUp() {
        every { remoteDbManagerMock.isSignedIn(any(), any()) } returns false
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun projectIdEmpty_shouldRequestLoginActComeUp() {
        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns ""
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun userIdEmpty_shouldRequestLoginActComeUp() {
        every { loginInfoManagerMock.getSignedInUserIdOrEmpty() } returns ""
        startCheckLoginAndCheckNextActivity(RequestLoginActivity::class.java)
    }

    @Test
    fun userIsLogged_shouldDashboardActComeUp() {
        every { remoteDbManagerMock.isSignedIn(any(), any()) } returns true
        every { loginInfoManagerMock.getSignedInProjectIdOrEmpty() } returns "project"
        every { loginInfoManagerMock.getSignedInUserIdOrEmpty() } returns "user"

        startCheckLoginAndCheckNextActivity(DashboardActivity::class.java)
    }

    private fun startCheckLoginAndCheckNextActivity(clazzNextActivity: Class<out Activity>) {
        val controller = createActivity<CheckLoginFromMainLauncherActivity>()
        val activity = controller.get()
        controller.resume().visible()
        assertActivityStarted(clazzNextActivity, activity)
    }
}
