package com.simprints.logging.persistent

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.simprints.logging.persistent.database.DbLogEntry
import com.simprints.logging.persistent.database.LogEntryDao
import com.simprints.logging.persistent.tools.ScopeProvider
import com.simprints.logging.persistent.tools.TimestampProvider
import com.simprints.testtools.common.coroutines.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabasePersistentLoggerTest {
    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @MockK
    private lateinit var logEntryDao: LogEntryDao

    @MockK
    private lateinit var timestampProvider: TimestampProvider

    @MockK
    private lateinit var scopeProvider: ScopeProvider

    private lateinit var persistentLogger: DatabasePersistentLogger

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        coJustRun { logEntryDao.save(any()) }
        coJustRun { logEntryDao.prune(any()) }
        coJustRun { logEntryDao.deleteAll() }

        every { timestampProvider.nowMs() } returns 0L
        every { scopeProvider.dispatcherIO } returns testCoroutineRule.testCoroutineDispatcher
        every { scopeProvider.externalScope } returns CoroutineScope(testCoroutineRule.testCoroutineDispatcher)

        persistentLogger = DatabasePersistentLogger(
            logDao = logEntryDao,
            timestampProvider = timestampProvider,
            scopeProvider = scopeProvider,
        )
    }

    @Test
    fun `saves and prunes log entries when logging`() = runTest {
        persistentLogger.log(LogEntryType.Intent, "title", "body")

        coVerify {
            logEntryDao.save(
                coWithArg {
                    it.timestampMs == 0L && it.title == "title" && it.body == "body"
                },
            )
        }
        coVerify { logEntryDao.prune(any()) }
    }

    @Test
    fun `saves and prunes log entries when logging with timestamp`() = runTest {
        persistentLogger.log(LogEntryType.Intent, 1L, "title", "body")

        coVerify {
            logEntryDao.save(
                coWithArg {
                    it.timestampMs == 1L && it.title == "title" && it.body == "body"
                },
            )
        }
        coVerify { logEntryDao.prune(any()) }
    }

    @Test
    fun `saves and prunes log entries when logging sync`() {
        persistentLogger.logSync(LogEntryType.Intent, "title", "body")

        coVerify {
            logEntryDao.save(
                coWithArg {
                    it.timestampMs == 0L && it.title == "title" && it.body == "body"
                },
            )
        }
        coVerify { logEntryDao.prune(any()) }
    }

    @Test
    fun `saves and prunes log entries when logging sync with timestamp`() {
        persistentLogger.logSync(LogEntryType.Intent, 1L, "title", "body")

        coVerify {
            logEntryDao.save(
                coWithArg {
                    it.timestampMs == 1L && it.title == "title" && it.body == "body"
                },
            )
        }
        coVerify { logEntryDao.prune(any()) }
    }

    @Test
    fun `delegates fetch to dao`() = runTest {
        coEvery { logEntryDao.getByType(any()) } returns listOf(
            DbLogEntry(0L, 0L, 1L, "Intent", "title", "body"),
        )
        val result = persistentLogger.get(LogEntryType.Intent)

        assertThat(result.first()).isEqualTo(
            LogEntry(1L, LogEntryType.Intent, "title", "body"),
        )
    }

    @Test
    fun `delegates clearing to dao`() = runTest {
        persistentLogger.clear()
        coVerify { logEntryDao.deleteAll() }
    }
}
