package com.simprints.face.capture.usecases

import android.content.SharedPreferences
import com.simprints.face.capture.usecases.GetAndTurnOffPreparationInstructionsShowingUseCase.Companion.INSTRUCTIONS_SHOWING_PREFERENCE_KEY
import com.simprints.infra.security.SecurityManager
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAndTurnOffPreparationInstructionsShowingUseCaseTest {

    @MockK
    private lateinit var securityManager: SecurityManager

    @MockK
    private lateinit var prefs: SharedPreferences

    private lateinit var getAndTurnOffPreparationInstructionsShowing: GetAndTurnOffPreparationInstructionsShowingUseCase

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { securityManager.buildEncryptedSharedPreferences(any()) } returns prefs

        getAndTurnOffPreparationInstructionsShowing = GetAndTurnOffPreparationInstructionsShowingUseCase(securityManager)
    }

    @Test
    fun `should return true and write false to preferences when instructions are showing`() = runTest {
        every { prefs.getBoolean(INSTRUCTIONS_SHOWING_PREFERENCE_KEY, any()) } returns true
        val editor = mockk<SharedPreferences.Editor>(relaxed = true)
        every { prefs.edit() } returns editor

        val result = getAndTurnOffPreparationInstructionsShowing()

        assertTrue(result)
        verify { editor.putBoolean(INSTRUCTIONS_SHOWING_PREFERENCE_KEY, false) }
    }

    @Test
    fun `should return false and not write to preferences when instructions are not showing`() = runTest {
        every { prefs.getBoolean(INSTRUCTIONS_SHOWING_PREFERENCE_KEY, any()) } returns false

        val result = getAndTurnOffPreparationInstructionsShowing()

        assertFalse(result)
        verify(exactly = 0) { prefs.edit() }
    }
}
