package com.simprints.id.activities.checkLogin

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.id.activities.checkLogin.openedByMainLauncher.CheckLoginFromMainLauncherPresenter
import com.simprints.id.commontesttools.di.TestAppModule
import com.simprints.id.domain.alert.AlertType
import com.simprints.id.exceptions.unexpected.RootedDeviceException
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.testtools.common.di.DependencyRule
import com.simprints.testtools.common.syntax.mock
import com.simprints.testtools.common.syntax.spy
import com.simprints.testtools.common.syntax.verifyOnce
import com.simprints.testtools.common.syntax.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class)
class CheckLoginFromMainLauncherPresenterTest {

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()

    private val appModule by lazy {
        TestAppModule(
            app,
            crashReportManagerRule = DependencyRule.MockRule,
            deviceManagerRule = DependencyRule.MockRule
        )
    }

    private lateinit var presenterSpy: CheckLoginFromMainLauncherPresenter

    @Before
    fun setUp() {
        UnitTestConfig(this, appModule).fullSetup()
        presenterSpy = spy(CheckLoginFromMainLauncherPresenter(mock(), app.component))
    }

    @Test
    fun withRootedDevice_shouldLogException() {
        val exception = RootedDeviceException()
        whenever { presenterSpy.deviceManager.checkIfDeviceIsRooted() } thenThrow exception

        presenterSpy.start()

        verifyOnce(presenterSpy.crashReportManager) {
            logExceptionOrSafeException(exception)
        }
    }

    @Test
    fun withRootedDevice_shouldShowAlertScreen() {
        whenever(presenterSpy.deviceManager) { checkIfDeviceIsRooted() } thenThrow RootedDeviceException()

        presenterSpy.start()

        verifyOnce(presenterSpy.view) {
            openAlertActivityForError(AlertType.ROOTED_DEVICE)
        }
    }

}
