package com.simprints.feature.dashboard.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.store.models.Finger.LEFT_3RD_FINGER
import com.simprints.infra.config.store.models.Finger.LEFT_4TH_FINGER
import com.simprints.infra.config.store.models.Finger.LEFT_5TH_FINGER
import com.simprints.infra.config.store.models.Finger.LEFT_INDEX_FINGER
import com.simprints.infra.config.store.models.Finger.LEFT_THUMB
import com.simprints.infra.config.store.models.Finger.RIGHT_3RD_FINGER
import com.simprints.infra.config.store.models.Finger.RIGHT_4TH_FINGER
import com.simprints.infra.config.store.models.Finger.RIGHT_5TH_FINGER
import com.simprints.infra.config.store.models.Finger.RIGHT_INDEX_FINGER
import com.simprints.infra.config.store.models.Finger.RIGHT_THUMB
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
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
        coEvery { getProjectConfiguration().fingerprint } returns fingerprintConfiguration
    }
    private val viewModel = FingerSelectionViewModel(configManager)

    @Test
    fun start_loadsSingleSdkFingerStatesCorrectly() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.nec } returns null

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(1)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(2)
        assertThat(fingerSelections?.first()?.items).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2),
                FingerSelectionItem(RIGHT_THUMB, 2)
            )
        ).inOrder()
    }

    @Test
    fun start_loadsTwoSdksFingerStatesCorrectly() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.nec?.fingersToCapture } returns listOf(
            LEFT_INDEX_FINGER, LEFT_INDEX_FINGER, LEFT_INDEX_FINGER,
            RIGHT_INDEX_FINGER, RIGHT_INDEX_FINGER, RIGHT_INDEX_FINGER, RIGHT_INDEX_FINGER
        )

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(2)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(2)
        assertThat(fingerSelections?.first()?.items).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 1),
                FingerSelectionItem(RIGHT_THUMB, 2)
            )
        ).inOrder()
        assertThat(fingerSelections?.get(1)?.sdkName).isEqualTo("NEC")
        assertThat(fingerSelections?.get(1)?.items).hasSize(2)
        assertThat(fingerSelections?.get(1)?.items).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_INDEX_FINGER, 3),
                FingerSelectionItem(RIGHT_INDEX_FINGER, 4)
            )
        ).inOrder()
    }

    @Test
    fun scatteredFingers_areAggregated() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
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
        every { fingerprintConfiguration.nec } returns null

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(1)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(10)
        assertThat(fingerSelections?.first()?.items).containsExactlyElementsIn(
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
