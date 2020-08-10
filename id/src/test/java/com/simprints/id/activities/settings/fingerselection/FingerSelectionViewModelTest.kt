package com.simprints.id.activities.settings.fingerselection

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.data.prefs.PreferencesManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Rule
import org.junit.Test

class FingerSelectionViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val prefsMock: PreferencesManager = mockk()
    private val viewModel = FingerSelectionViewModel(prefsMock)

    @Test
    fun start_loadsStartingFingerStateCorrectly() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.RIGHT_THUMB, 2, true))).inOrder()
    }

    @Test
    fun addFinger_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.addNewFinger()

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.RIGHT_THUMB, 2, true),
            FingerSelectionItem(FingerIdentifier.LEFT_INDEX_FINGER, 1, true))).inOrder()
    }

    @Test
    fun removeItem_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.removeItem(1)

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.LEFT_INDEX_FINGER, 1, true))).inOrder()
    }

    @Test
    fun moveItem_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.moveItem(0, 1)

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.RIGHT_THUMB, 2, true),
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.LEFT_INDEX_FINGER, 1, true))).inOrder()
    }

    @Test
    fun changeFingerSelection_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.changeFingerSelection(1, FingerIdentifier.RIGHT_INDEX_FINGER)

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.RIGHT_INDEX_FINGER, 2, true))).inOrder()
    }

    @Test
    fun changeQuantitySelection_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.changeQuantitySelection(1, 5)

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 2, false),
            FingerSelectionItem(FingerIdentifier.RIGHT_THUMB, 5, true))).inOrder()
    }

    @Test
    fun resetFingerItems_correctlyUpdatesState() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        viewModel.resetFingerItems()

        assertThat(viewModel.items.value).containsExactlyElementsIn(listOf(
            FingerSelectionItem(FingerIdentifier.LEFT_THUMB, 1, false))).inOrder()
    }

    @Test
    fun haveSettingsChanged_determinesCorrectValue() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.haveSettingsChanged()).isFalse()

        viewModel.addNewFinger()

        assertThat(viewModel.haveSettingsChanged()).isTrue()

        viewModel.removeItem(2)

        assertThat(viewModel.haveSettingsChanged()).isFalse()
    }

    @Test
    fun canSavePreference_withinLimit_returnsTrue() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB
        )
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.canSavePreference()).isTrue()
    }

    @Test
    fun canSavePreference_overLimit_returnsFalse() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB
        )
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()

        assertThat(viewModel.canSavePreference()).isFalse()
    }

    @Test
    fun savePreference_savesPreferenceCorrectly() {
        every { prefsMock.fingerprintsToCollect } returns listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB)
        every { prefsMock.getRemoteConfigFingerprintsToCollect() } returns listOf(FingerIdentifier.LEFT_THUMB)

        viewModel.start()
        viewModel.addNewFinger()

        viewModel.savePreference()

        val slot = slot<List<FingerIdentifier>>()
        verify { prefsMock.fingerprintsToCollect = capture(slot) }

        assertThat(slot.captured).containsExactlyElementsIn(listOf(
            FingerIdentifier.LEFT_THUMB, FingerIdentifier.LEFT_THUMB,
            FingerIdentifier.RIGHT_THUMB, FingerIdentifier.RIGHT_THUMB,
            FingerIdentifier.LEFT_INDEX_FINGER)).inOrder()
    }
}
