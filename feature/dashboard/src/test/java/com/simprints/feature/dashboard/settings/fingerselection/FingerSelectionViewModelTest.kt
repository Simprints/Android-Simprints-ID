package com.simprints.feature.dashboard.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.Finger.*
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class FingerSelectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configManager = mockk<ConfigManager>(relaxed = true) {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
    }
    private val viewModel = FingerSelectionViewModel(
        configManager,
    )

    @Test
    fun start_loadsFingerStateCorrectly() {
        every { fingerprintConfiguration.fingersToCapture } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )

        viewModel.start()

        assertThat(viewModel.fingerSelections.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2),
                FingerSelectionItem(RIGHT_THUMB, 2)
            )
        ).inOrder()
    }

    @Test
    fun scatteredFingers_areAggregated() {
        every { fingerprintConfiguration.fingersToCapture } returns listOf(
            LEFT_THUMB,
            RIGHT_THUMB,
            RIGHT_INDEX_FINGER,
            LEFT_3RD_FINGER,
            LEFT_3RD_FINGER,
            LEFT_4TH_FINGER,
            LEFT_INDEX_FINGER,
            RIGHT_5TH_FINGER,
            LEFT_5TH_FINGER,
            LEFT_3RD_FINGER,
            LEFT_4TH_FINGER,
            RIGHT_5TH_FINGER,
            RIGHT_5TH_FINGER,
            LEFT_5TH_FINGER,
            RIGHT_4TH_FINGER,
            LEFT_4TH_FINGER,
            RIGHT_3RD_FINGER,
            RIGHT_4TH_FINGER,
            RIGHT_5TH_FINGER,
            LEFT_5TH_FINGER,
            RIGHT_INDEX_FINGER,
            RIGHT_4TH_FINGER,
            LEFT_4TH_FINGER,
            LEFT_5TH_FINGER,
            RIGHT_3RD_FINGER,
            LEFT_5TH_FINGER,
            RIGHT_4TH_FINGER,
            RIGHT_5TH_FINGER,
            LEFT_INDEX_FINGER,
            RIGHT_3RD_FINGER,
        )

        viewModel.start()

        assertThat(viewModel.fingerSelections.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 1),
                FingerSelectionItem(RIGHT_THUMB, 1),
                FingerSelectionItem(RIGHT_INDEX_FINGER, 2),
                FingerSelectionItem(LEFT_3RD_FINGER, 3),
                FingerSelectionItem(LEFT_4TH_FINGER, 4),
                FingerSelectionItem(LEFT_INDEX_FINGER, 2),
                FingerSelectionItem(RIGHT_5TH_FINGER, 5),
                FingerSelectionItem(LEFT_5TH_FINGER, 5),
                FingerSelectionItem(RIGHT_4TH_FINGER, 4),
                FingerSelectionItem(RIGHT_3RD_FINGER, 3),
            )
        ).inOrder()
    }
}
