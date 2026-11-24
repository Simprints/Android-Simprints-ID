package com.simprints.feature.dashboard.settings.fingerselection

import com.google.common.truth.Truth.*
import com.simprints.core.domain.common.TemplateIdentifier
import com.simprints.feature.dashboard.tools.ViewModelScopeRule
import com.simprints.infra.config.store.ConfigRepository
import com.simprints.infra.config.store.models.FingerprintConfiguration
import com.simprints.infra.logging.LoggingConstants
import com.simprints.infra.logging.LoggingConstants.CrashReportTag.SETTINGS
import com.simprints.infra.logging.Simber
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FingerSelectionViewModelTest {
    @get:Rule(order = 0)
    val testCoroutineRule = TestCoroutineRule()

    // Inside TestCoroutineRule: scopes are cancelled while the test Main dispatcher is still set
    @get:Rule(order = 1)
    val viewModels = ViewModelScopeRule()

    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val configRepository = mockk<ConfigRepository>(relaxed = true) {
        coEvery { getProjectConfiguration().fingerprint } returns fingerprintConfiguration
    }
    private lateinit var viewModel: FingerSelectionViewModel

    @Before
    fun setUp() {
        mockkObject(Simber)
        justRun { Simber.w(any<String>(), any<Throwable>(), any<LoggingConstants.CrashReportTag>()) }

        // Created after TestCoroutineRule has set the test Main dispatcher: the base class starts its action loop in init.
        viewModel = viewModels.track(FingerSelectionViewModel(configRepository))
    }

    @After
    fun tearDown() {
        unmockkObject(Simber)
    }

    @Test
    fun `load builds the sections for a single SDK`() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
        )
        every { fingerprintConfiguration.nec } returns null

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection(
                    "SimMatcher",
                    listOf(
                        FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 2),
                        FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `load builds the sections for two SDKs`() {
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

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection(
                    "SimMatcher",
                    listOf(
                        FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1),
                        FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 2),
                    ),
                ),
                FingerSelectionSection(
                    "NEC",
                    listOf(
                        FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 3),
                        FingerSelectionItem(TemplateIdentifier.RIGHT_INDEX_FINGER, 4),
                    ),
                ),
            ),
        )
    }

    @Test
    fun `scattered fingers are aggregated in first-seen order`() {
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

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection(
                    "SimMatcher",
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
                ),
            ),
        )
    }

    @Test
    fun `no fingerprint configuration loads an empty list`() {
        coEvery { configRepository.getProjectConfiguration().fingerprint } returns null
        assertThat(viewModel.uiState.value.sections).isNull()

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(emptyList<FingerSelectionSection>())
    }

    @Test
    fun `no SDK configuration loads an empty list`() {
        every { fingerprintConfiguration.secugenSimMatcher } returns null
        every { fingerprintConfiguration.nec } returns null

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(emptyList<FingerSelectionSection>())
    }

    @Test
    fun `unknown fingers are skipped and the known ones counted`() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.NONE,
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.NONE,
        )
        every { fingerprintConfiguration.nec } returns null

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection("SimMatcher", listOf(FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 2))),
            ),
        )
        verify(exactly = 1) { Simber.w(any<String>(), any<Throwable>(), SETTINGS) }
    }

    @Test
    fun `a section with only unknown fingers stays with an empty list`() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.NONE,
            TemplateIdentifier.NONE,
        )
        every { fingerprintConfiguration.nec?.fingersToCapture } returns listOf(TemplateIdentifier.LEFT_INDEX_FINGER)

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection("SimMatcher", emptyList()),
                FingerSelectionSection("NEC", listOf(FingerSelectionItem(TemplateIdentifier.LEFT_INDEX_FINGER, 1))),
            ),
        )
        verify(exactly = 1) { Simber.w(any<String>(), any<Throwable>(), SETTINGS) }
    }

    @Test
    fun `known fingers only log no warning`() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(TemplateIdentifier.LEFT_THUMB)
        every { fingerprintConfiguration.nec } returns null

        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(FingerSelectionSection("SimMatcher", listOf(FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 1)))),
        )
        verify(exactly = 0) { Simber.w(any<String>(), any<Throwable>(), any<LoggingConstants.CrashReportTag>()) }
    }

    @Test
    fun `each load re-reads the configuration and replaces the sections without changing earlier ones`() {
        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.LEFT_THUMB,
        )
        every { fingerprintConfiguration.nec } returns null
        viewModel.onAction(FingerSelectionUiAction.Load)
        val firstSections = viewModel.uiState.value.sections

        every { fingerprintConfiguration.secugenSimMatcher?.fingersToCapture } returns listOf(
            TemplateIdentifier.LEFT_THUMB,
            TemplateIdentifier.RIGHT_THUMB,
            TemplateIdentifier.LEFT_THUMB,
        )
        viewModel.onAction(FingerSelectionUiAction.Load)

        assertThat(viewModel.uiState.value.sections).isEqualTo(
            listOf(
                FingerSelectionSection(
                    "SimMatcher",
                    listOf(
                        FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 2),
                        FingerSelectionItem(TemplateIdentifier.RIGHT_THUMB, 1),
                    ),
                ),
            ),
        )
        assertThat(firstSections).isEqualTo(
            listOf(
                FingerSelectionSection(
                    "SimMatcher",
                    listOf(FingerSelectionItem(TemplateIdentifier.LEFT_THUMB, 2)),
                ),
            ),
        )
    }
}
