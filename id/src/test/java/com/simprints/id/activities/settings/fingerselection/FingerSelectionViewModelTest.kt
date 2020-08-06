package com.simprints.id.activities.settings.fingerselection

import com.simprints.id.data.prefs.PreferencesManager
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class FingerSelectionViewModelTest {


    private val prefsMock: PreferencesManager = mockk()
    private val viewModel = FingerSelectionViewModel(prefsMock)

    @Test
    fun start_loadsStartingFingerStateCorrectly() {

    }
}
