package com.simprints.id.activities.setup

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.play.core.ktx.requestProgressFlow
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.verifyExactly
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SetupViewModelTest {

    @MockK lateinit var splitInstallManagerMock: SplitInstallManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun noModalitiesInstalled_shouldStartInstallFromSplitInstallManager() {
        val modalityList = listOf("fingerprint","face")
        every { splitInstallManagerMock.installedModules } returns emptySet()

        val viewModel = SetupViewModel()
        viewModel.start(splitInstallManagerMock, modalityList)

        verify(exactly = 1) { splitInstallManagerMock.startInstall(any()) }
    }

    @Test
    fun modalitiesInstalled_shouldHaveCorrectViewState() {
        val modalityList = listOf("fingerprint","face")
        every { splitInstallManagerMock.installedModules } returns setOf("fingerprint","face")

        val viewModel = SetupViewModel()
        viewModel.start(splitInstallManagerMock, modalityList)

        assertThat(viewModel.getViewStateLiveData().value).isEqualTo(SetupActivity.ViewState.ModalitiesInstalled)
    }

    @Test
    fun modalityDownloadPending_shouldUpdateCorrectViewState() {
        runBlocking {
            val mockSessionState: SplitInstallSessionState = mockk()
            every { mockSessionState.status() } returns SplitInstallSessionStatus.PENDING
            coEvery { splitInstallManagerMock.requestProgressFlow() } returns flowOf(mockSessionState)

            val viewModel = SetupViewModel()
            viewModel.monitorDownloadProgress(splitInstallManagerMock)

            assertThat(viewModel.getViewStateLiveData().value).isEqualTo(SetupActivity.ViewState.StartingDownload)
        }
    }
}
