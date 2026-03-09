package com.simprints.infra.aichat

import com.google.common.truth.Truth.assertThat
import com.simprints.infra.aichat.database.ChatDao
import com.simprints.infra.aichat.database.ChatMessageEntity
import com.simprints.infra.aichat.database.ChatSessionEntity
import com.simprints.infra.aichat.engine.ChatEngine
import com.simprints.infra.aichat.engine.ChatEngineSelector
import com.simprints.infra.aichat.model.ChatContext
import com.simprints.infra.aichat.model.ChatRole
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatRepositoryTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var chatDao: ChatDao

    @MockK
    private lateinit var engineSelector: ChatEngineSelector

    @RelaxedMockK
    private lateinit var chatEngine: ChatEngine

    private lateinit var repository: ChatRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coJustRun { chatDao.insertSession(any()) }
        coJustRun { chatDao.insertMessage(any()) }

        repository = ChatRepository(chatDao, engineSelector)
    }

    @Test
    fun `sendMessage persists user message and streams response`() = runTest {
        val sessionId = "test-session"
        coEvery { chatDao.getMessages(sessionId) } returns listOf(
            ChatMessageEntity(1, sessionId, "USER", "Hello", 1000L),
        )
        coEvery { engineSelector.selectEngine() } returns chatEngine
        every { chatEngine.chat(any(), any()) } returns flowOf("Hi", " there!")

        val chunks = repository.sendMessage(sessionId, "Hello", ChatContext()).toList()

        assertThat(chunks).containsExactly("Hi", " there!")
        coVerify { chatDao.insertSession(match { it.id == sessionId }) }
        coVerify { chatDao.insertMessage(match { it.role == "USER" && it.content == "Hello" }) }
        // Assistant message saved on completion
        coVerify { chatDao.insertMessage(match { it.role == "ASSISTANT" && it.content == "Hi there!" }) }
    }

    @Test
    fun `createSession returns new session ID`() = runTest {
        val id = repository.createSession()

        assertThat(id).isNotEmpty()
        coVerify { chatDao.insertSession(match { it.id == id }) }
    }

    @Test
    fun `getSession returns null when session not found`() = runTest {
        coEvery { chatDao.getSession("nonexistent") } returns null

        val result = repository.getSession("nonexistent")

        assertThat(result).isNull()
    }

    @Test
    fun `getSession returns session with messages`() = runTest {
        val sessionId = "test-session"
        coEvery { chatDao.getSession(sessionId) } returns ChatSessionEntity(sessionId, 1000L)
        coEvery { chatDao.getMessages(sessionId) } returns listOf(
            ChatMessageEntity(1, sessionId, "USER", "Hello", 1000L),
            ChatMessageEntity(2, sessionId, "ASSISTANT", "Hi!", 1001L),
        )

        val result = repository.getSession(sessionId)

        assertThat(result).isNotNull()
        assertThat(result!!.id).isEqualTo(sessionId)
        assertThat(result.messages).hasSize(2)
        assertThat(result.messages[0].role).isEqualTo(ChatRole.USER)
        assertThat(result.messages[1].role).isEqualTo(ChatRole.ASSISTANT)
    }

    @Test
    fun `deleteSession removes session and messages`() = runTest {
        coJustRun { chatDao.deleteMessages(any()) }
        coJustRun { chatDao.deleteSession(any()) }

        repository.deleteSession("test-session")

        coVerify { chatDao.deleteMessages("test-session") }
        coVerify { chatDao.deleteSession("test-session") }
    }

    @Test
    fun `clearAllHistory removes everything`() = runTest {
        coJustRun { chatDao.deleteAllMessages() }
        coJustRun { chatDao.deleteAllSessions() }

        repository.clearAllHistory()

        coVerify { chatDao.deleteAllMessages() }
        coVerify { chatDao.deleteAllSessions() }
    }
}
