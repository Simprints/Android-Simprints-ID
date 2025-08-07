package com.simprints.infra.logging.usecases

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class UpdateAndGetVersionHistoryTest {
    @MockK
    lateinit var mockContext: Context

    @MockK
    private lateinit var mockSharedPreferences: SharedPreferences

    @MockK
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var updateAndGetVersionHistoryUseCase: UpdateAndGetVersionHistoryUseCase

    private lateinit var slot: CapturingSlot<String>

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true, relaxUnitFun = true)

        every { mockContext.getSharedPreferences(any(), Context.MODE_PRIVATE) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor

        slot = slot()
        every { mockEditor.putString(any(), capture(slot)) } returns mockEditor

        updateAndGetVersionHistoryUseCase = UpdateAndGetVersionHistoryUseCase()
    }

    @Test
    fun `adds current version and returns it when cache is empty `() {
        verifyCorrectCachedVersions(
            currentVersion = "10010701",
            cachedVersions = null,
            expectedVersions = "10010701",
        )
    }

    @Test
    fun `returns existing versions without change when current version is already cached and is the first`() {
        verifyCorrectCachedVersions(
            currentVersion = "10010701",
            cachedVersions = "10010701;10010601",
            expectedVersions = "10010701;10010601",
            shouldWriteCache = false,
        )
    }

    @Test
    fun `prepends current version when cache is not empty and current version is new`() {
        verifyCorrectCachedVersions(
            currentVersion = "10010701",
            cachedVersions = "10010601;10010501",
            expectedVersions = "10010701;10010601;10010501",
        )
    }

    @Test
    fun `truncates oldest versions when current version is new and adding it exceeds MAX_VALUE_LENGTH `() {
        verifyCorrectCachedVersions(
            currentVersion = "2025.3.0+107.1",
            // Cached is already at the limit of the analytics keys
            cachedVersions = "2025.2.0+107.1;2025.1.0+107.1;2024.4.0+107.1;2024.3.0+107.1;2024.2.0+107.1;2024.1.0+107.1",
            expectedVersions = "2025.3.0+107.1;2025.2.0+107.1;2025.1.0+107.1;2024.4.0+107.1;2024.3.0+107.1;2024.2.0+107.1",
        )
    }

    private fun verifyCorrectCachedVersions(
        currentVersion: String,
        cachedVersions: String?,
        expectedVersions: String,
        shouldWriteCache: Boolean = true,
    ) {
        every { mockSharedPreferences.getString(any(), any()) } returns cachedVersions

        val result = updateAndGetVersionHistoryUseCase(mockContext, currentVersion)

        assertThat(result).isEqualTo(expectedVersions)
        verify(exactly = if (shouldWriteCache) 1 else 0) { mockEditor.putString(any(), eq(expectedVersions)) }
    }
}
