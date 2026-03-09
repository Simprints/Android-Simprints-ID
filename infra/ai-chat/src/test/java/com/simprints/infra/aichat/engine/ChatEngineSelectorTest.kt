package com.simprints.infra.aichat.engine

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatEngineSelectorTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var cloudEngine: FirebaseChatEngine

    @MockK
    private lateinit var offlineEngine: OfflineFaqEngine

    private lateinit var selector: ChatEngineSelector

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        selector = ChatEngineSelector(cloudEngine, offlineEngine)
    }

    @Test
    fun `selects cloud engine when available`() = runTest {
        coEvery { cloudEngine.isAvailable() } returns true

        val result = selector.selectEngine()

        assertThat(result).isEqualTo(cloudEngine)
    }

    @Test
    fun `selects offline engine when cloud is unavailable`() = runTest {
        coEvery { cloudEngine.isAvailable() } returns false

        val result = selector.selectEngine()

        assertThat(result).isEqualTo(offlineEngine)
    }
}
