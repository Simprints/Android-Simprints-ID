package com.simprints.feature.dashboard.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.*
import com.simprints.core.domain.reference.TemplateIdentifier
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.config.sync.ConfigManager
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
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
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
        )
        every { fingerprintConfiguration.nec } returns null

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(1)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(2)
        assertThat(fingerSelections?.first()?.items)
            .containsExactlyElementsIn(
                listOf(
                    FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 2),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
                ),
            ).inOrder()
    }

    @Test
    fun start_loadsTwoSdksFingerStatesCorrectly() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
        )
        every { fingerprintConfiguration.nec?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
        )

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(2)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(2)
        assertThat(fingerSelections?.first()?.items)
            .containsExactlyElementsIn(
                listOf(
                    FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
                ),
            ).inOrder()
        assertThat(fingerSelections?.get(1)?.sdkName).isEqualTo("NEC")
        assertThat(fingerSelections?.get(1)?.items).hasSize(2)
        assertThat(fingerSelections?.get(1)?.items)
            .containsExactlyElementsIn(
                listOf(
                    FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 3),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_INDEX_FINGER, 4),
                ),
            ).inOrder()
    }

    @Test
    fun scatteredFingers_areAggregated() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.LEFT_3RD_FINGER,
            TemplateIdentifier.LEFT_3RD_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER,
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.LEFT_3RD_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER,
            TemplateIdentifier.RIGHT_3RD_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER,
            TemplateIdentifier.LEFT_4TH_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_3RD_FINGER,
            TemplateIdentifier.LEFT_5TH_FINGER,
            TemplateIdentifier.RIGHT_4TH_FINGER,
            TemplateIdentifier.RIGHT_5TH_FINGER,
            TemplateIdentifier.LEFT_INDEX_FINGER,
            TemplateIdentifier.RIGHT_3RD_FINGER,
        )
        every { fingerprintConfiguration.nec } returns null

        viewModel.start()

        val fingerSelections: List<FingerSelectionSection>? = viewModel.fingerSelections.value
        assertThat(fingerSelections).hasSize(1)
        assertThat(fingerSelections?.first()?.sdkName).isEqualTo("SimMatcher")
        assertThat(fingerSelections?.first()?.items).hasSize(10)
        assertThat(fingerSelections?.first()?.items)
            .containsExactlyElementsIn(
                listOf(
                    FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 1),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_INDEX_FINGER, 2),
                    FingerSelectionItem(TemplateIdentifier.LEFT_3RD_FINGER, 3),
                    FingerSelectionItem(TemplateIdentifier.LEFT_4TH_FINGER, 4),
                    FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 2),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_5TH_FINGER, 5),
                    FingerSelectionItem(TemplateIdentifier.LEFT_5TH_FINGER, 5),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_4TH_FINGER, 4),
                    FingerSelectionItem(TemplateIdentifier.RIGHT_3RD_FINGER, 3),
                ),
            ).inOrder()
    }
}
