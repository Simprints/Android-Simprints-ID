package com.simprints.infra.aichat.engine

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.aichat.database.FaqDao
import com.simprints.infra.aichat.database.FaqEntryEntity
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatMessage
import com.simprints.infra.aichat.model.ChatRole
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OfflineFaqEngineTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var faqDao: FaqDao

    private lateinit var engine: OfflineFaqEngine

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        engine = OfflineFaqEngine(faqDao)
    }

    @Test
    fun `is always available`() = runTest {
        assertThat(engine.isAvailable()).isTrue()
    }

    @Test
    fun `returns matching FAQ entries when found`() = runTest {
        val faq = FaqEntryEntity(
            id = 1,
            question = "How do I connect the scanner?",
            answer = "Turn on Bluetooth and bring the scanner close.",
            keywords = "scanner connect bluetooth",
            category = "hardware",
        )
        coEvery { faqDao.search(any()) } returns listOf(faq)

        val messages = listOf(
            ChatMessage(ChatRole.USER, "scanner not connecting", System.currentTimeMillis()),
        )

        val results = engine.chat(messages, ChatContext()).toList()

        assertThat(results).hasSize(1)
        assertThat(results.first()).contains("How do I connect the scanner?")
        assertThat(results.first()).contains("Turn on Bluetooth")
    }

    @Test
    fun `returns no match response when no FAQ entries found`() = runTest {
        coEvery { faqDao.search(any()) } returns emptyList()

        val messages = listOf(
            ChatMessage(ChatRole.USER, "xyzzy foobar baz", System.currentTimeMillis()),
        )

        val results = engine.chat(messages, ChatContext()).toList()

        assertThat(results).hasSize(1)
        assertThat(results.first()).contains("offline mode")
    }

    @Test
    fun `returns empty when no messages provided`() = runTest {
        val results = engine.chat(emptyList(), ChatContext()).toList()
        assertThat(results).isEmpty()
    }
}
