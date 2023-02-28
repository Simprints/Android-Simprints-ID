package com.simprints.feature.dashboard.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Finger.*
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FingerSelectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val deviceConfiguration = mockk<DeviceConfiguration>()
    private val configManager = mockk<ConfigManager>(relaxed = true) {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
        coEvery { getDeviceConfiguration() } returns deviceConfiguration
    }
    private val viewModel = FingerSelectionViewModel(
        configManager,
    )

    @Test
    fun start_loadsStartingFingerStateCorrectly() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.fingerSelections.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2),
                FingerSelectionItem(RIGHT_THUMB, 2)
            )
        ).inOrder()
    }
}
