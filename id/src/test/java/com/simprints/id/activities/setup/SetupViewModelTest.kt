package com.simprints.id.activities.setup

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.common.truth.Truth.assertThat
import com.simprints.id.activities.setup.SetupActivity.ViewState.*
import com.simprints.id.data.analytics.crashreport.CrashReportManager
import com.simprints.id.testtools.TestApplication
import com.simprints.id.testtools.UnitTestConfig
import com.simprints.id.tools.device.DeviceManager
import com.simprints.testtools.common.livedata.testObserver
import com.simprints.testtools.unit.robolectric.ShadowAndroidXMultiDex
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(application = TestApplication::class, shadows = [ShadowAndroidXMultiDex::class])
class SetupViewModelTest {

    @MockK lateinit var splitInstallManagerMock: SplitInstallManager
    @MockK lateinit var deviceManagerMock: DeviceManager
    @MockK lateinit var crashReportManagerMock: CrashReportManager

    private lateinit var isConnectedUpdates: MutableLiveData<Boolean>

    @Before
    fun setUp() {
        UnitTestConfig(this).coroutinesMainThread()
        MockKAnnotations.init(this)
        isConnectedUpdates = MutableLiveData()
        every { deviceManagerMock.isConnectedLiveData } returns isConnectedUpdates
    }

    @Test
    fun noModalitiesInstalled_shouldStartInstallFromSplitInstallManager() {
        val modalityList = listOf("fingerprint","face")
        every { splitInstallManagerMock.installedModules } returns emptySet()

        val viewModel = SetupViewModel(deviceManagerMock, crashReportManagerMock)
        viewModel.start(splitInstallManagerMock, modalityList)

        verify(exactly = 1) { splitInstallManagerMock.startInstall(any()) }
    }

    @Test
    fun modalitiesInstalled_shouldHaveCorrectViewState() {
        val modalityList = listOf("fingerprint","face")
        every { splitInstallManagerMock.installedModules } returns setOf("fingerprint","face")

        val viewModel = SetupViewModel(deviceManagerMock, crashReportManagerMock)
        viewModel.start(splitInstallManagerMock, modalityList)

        assertThat(viewModel.getViewStateLiveData().testObserver().observedValues.last())
            .isEqualTo(ModalitiesInstalled)
    }

    @Test
    fun deviceOffline_shouldHaveCorrectViewState() {
        isConnectedUpdates.value = false

        val viewModel = SetupViewModel(deviceManagerMock, crashReportManagerMock)

        assertThat(viewModel.getDeviceNetworkLiveData().testObserver().observedValues.last())
            .isEqualTo(DeviceOffline)
    }

    @Test
    fun deviceOnline_shouldHaveCorrectViewState() {
        isConnectedUpdates.value = true

        val viewModel = SetupViewModel(deviceManagerMock, crashReportManagerMock)

        assertThat(viewModel.getDeviceNetworkLiveData().testObserver().observedValues.last())
            .isEqualTo(DeviceOnline)
    }
}
