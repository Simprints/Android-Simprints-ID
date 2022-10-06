package com.simprints.id.activities.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.infra.config.ConfigManager
import com.simprints.infra.config.domain.models.DeviceConfiguration
import com.simprints.infra.config.domain.models.Finger.*
import com.simprints.infra.config.domain.models.FingerprintConfiguration
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FingerSelectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val fingerprintConfiguration = mockk<FingerprintConfiguration>()
    private val deviceConfiguration = mockk<DeviceConfiguration>()
    private val configManager = mockk<ConfigManager> {
        coEvery { getProjectConfiguration() } returns mockk {
            every { fingerprint } returns fingerprintConfiguration
        }
        coEvery { getDeviceConfiguration() } returns deviceConfiguration
    }
    private val viewModel = FingerSelectionViewModel(configManager, UnconfinedTestDispatcher())

    @Test
    fun start_loadsStartingFingerStateCorrectly() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(RIGHT_THUMB, 2, true)
            )
        ).inOrder()
    }

    @Test
    fun addFinger_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.addNewFinger()

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(RIGHT_THUMB, 2, true),
                FingerSelectionItem(LEFT_INDEX_FINGER, 1, true)
            )
        ).inOrder()
    }

    @Test
    fun removeItem_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB,
            LEFT_INDEX_FINGER
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.removeItem(1)

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(LEFT_INDEX_FINGER, 1, true)
            )
        ).inOrder()
    }

    @Test
    fun moveItem_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB,
            LEFT_INDEX_FINGER
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.moveItem(0, 1)

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(RIGHT_THUMB, 2, true),
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(LEFT_INDEX_FINGER, 1, true)
            )
        ).inOrder()
    }

    @Test
    fun changeFingerSelection_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.changeFingerSelection(1, RIGHT_INDEX_FINGER)

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(RIGHT_INDEX_FINGER, 2, true)
            )
        ).inOrder()
    }

    @Test
    fun changeQuantitySelection_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.changeQuantitySelection(1, 5)

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 2, false),
                FingerSelectionItem(RIGHT_THUMB, 5, true)
            )
        ).inOrder()
    }

    @Test
    fun resetFingerItems_correctlyUpdatesState() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB,
            LEFT_INDEX_FINGER
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        viewModel.resetFingerItems()

        assertThat(viewModel.items.value).containsExactlyElementsIn(
            listOf(
                FingerSelectionItem(LEFT_THUMB, 1, false)
            )
        ).inOrder()
    }

    @Test
    fun haveSettingsChanged_determinesCorrectValue() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.haveSettingsChanged()).isFalse()

        viewModel.addNewFinger()

        assertThat(viewModel.haveSettingsChanged()).isTrue()

        viewModel.removeItem(2)

        assertThat(viewModel.haveSettingsChanged()).isFalse()

        viewModel.addNewFinger()

        viewModel.savePreference()

        assertThat(viewModel.haveSettingsChanged()).isFalse()
    }

    @Test
    fun canSavePreference_withinLimit_returnsTrue() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB,
            LEFT_THUMB, LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.canSavePreference()).isTrue()
    }

    @Test
    fun canSavePreference_overLimit_returnsFalse() {
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB,
            LEFT_THUMB,
            LEFT_THUMB,
            RIGHT_THUMB,
            RIGHT_THUMB,
            RIGHT_THUMB,
            LEFT_THUMB,
            LEFT_THUMB,
            LEFT_THUMB,
            RIGHT_THUMB,
            RIGHT_THUMB,
            RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.canSavePreference()).isFalse()
    }

    @Test
    fun savePreference_savesPreference() = runTest {
        val updateConfigFn = slot<suspend (DeviceConfiguration) -> DeviceConfiguration>()
        coEvery { configManager.updateDeviceConfiguration(capture(updateConfigFn)) } returns Unit
        every { deviceConfiguration.fingersToCollect } returns listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB
        )
        every { fingerprintConfiguration.fingersToCapture } returns listOf(LEFT_THUMB)

        viewModel.start()
        viewModel.addNewFinger()

        viewModel.savePreference()


        val updatedConfig = updateConfigFn.captured(DeviceConfiguration("", listOf(), listOf()))
        val expectedFingersToCollect = listOf(
            LEFT_THUMB, LEFT_THUMB,
            RIGHT_THUMB, RIGHT_THUMB,
            LEFT_INDEX_FINGER
        )
        // Comparing string representation as when executing the lambda captured in the mock it will
        // not return an ArrayList but a LinkedHashMap.
        assertThat(updatedConfig.fingersToCollect.toString()).isEqualTo(expectedFingersToCollect.toString())
    }
}
