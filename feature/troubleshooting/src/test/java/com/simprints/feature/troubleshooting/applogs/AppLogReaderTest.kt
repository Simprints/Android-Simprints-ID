package com.simprints.feature.troubleshooting.applogs

import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.TimeZone

class AppLogReaderTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `returns empty list when there are no log files`() {
        assertThat(readRecentLogEntries(tempFolder.root, 10)).isEmpty()
    }

    @Test
    fun `returns nothing when max entries is zero`() {
        tempFolder.newFile("simprints.0.log").writeText("2026-10-26T14:10:59.100Z Info: (A) one\n")

        assertThat(readRecentLogEntries(tempFolder.root, 0)).isEmpty()
    }

    @Test
    fun `returns newest entries first across files and stops at the limit`() {
        tempFolder.newFile("simprints.0.log").apply {
            writeText(
                "2026-10-26T14:10:59.100Z Info: (A) a1\n" +
                    "2026-10-26T14:10:59.200Z Info: (A) a2\n" +
                    "2026-10-26T14:10:59.300Z Info: (A) a3\n",
            )
            setLastModified(1_000L)
        }
        tempFolder.newFile("simprints.1.log").apply {
            writeText(
                "2026-10-26T14:10:59.400Z Info: (A) b1\n" +
                    "2026-10-26T14:10:59.500Z Info: (A) b2\n",
            )
            setLastModified(2_000L)
        }

        assertThat(readRecentLogEntries(tempFolder.root, 3).map { it.message })
            .containsExactly("(A) b2", "(A) b1", "(A) a3")
            .inOrder()
    }

    @Test
    fun `keeps stack trace lines with the entry they belong to`() {
        tempFolder.newFile("simprints.0.log").writeText(
            "2026-10-26T14:10:59.100Z Info: (A) before\n" +
                "2026-10-26T14:10:59.200Z Error: (B) failed\n" +
                "java.lang.IllegalStateException: boom\n" +
                "    at com.example.Foo.bar(Foo.kt:1)\n" +
                "2026-10-26T14:10:59.300Z Info: (A) after\n",
        )

        val entries = readRecentLogEntries(tempFolder.root, 10)

        assertThat(entries.map { it.message }).containsExactly("(A) after", "(B) failed", "(A) before").inOrder()
        assertThat(entries[1].fullText).isEqualTo(
            "2026-10-26T14:10:59.200Z Error: (B) failed\n" +
                "java.lang.IllegalStateException: boom\n" +
                "    at com.example.Foo.bar(Foo.kt:1)",
        )
    }

    @Test
    fun `drops lines that come before the first entry`() {
        tempFolder.newFile("simprints.0.log").writeText(
            "orphan line\n2026-10-26T14:10:59.100Z Info: (A) first\n",
        )

        assertThat(readRecentLogEntries(tempFolder.root, 10).map { it.message })
            .containsExactly("(A) first")
    }

    @Test
    fun `matches filters by severity`() {
        val info = entry("Info", "(A) hello")
        val debug = entry("Debug", "(A) hello")

        assertThat(info.matches(setOf("Info"), "")).isTrue()
        assertThat(debug.matches(setOf("Info"), "")).isFalse()
    }

    @Test
    fun `matches searches the whole entry case-insensitively`() {
        val error = AppLogEntry(
            timestamp = "2026-10-26T14:10:59.200Z",
            severity = "Error",
            message = "(B) failed",
            lines = listOf(
                "2026-10-26T14:10:59.200Z Error: (B) failed",
                "java.lang.IllegalStateException: boom",
            ),
        )

        assertThat(error.matches(setOf("Error"), "ILLEGALSTATE")).isTrue()
        assertThat(error.matches(setOf("Error"), "missing")).isFalse()
        assertThat(error.matches(setOf("Error"), "")).isTrue()
    }

    @Test
    fun `converts utc timestamps to the local zone`() {
        assertThat(toLocalTime("2026-10-26T14:10:59.729Z", TimeZone.getTimeZone("Asia/Tokyo")))
            .isEqualTo("23:10:59.729")
    }

    @Test
    fun `returns the original text when the timestamp cannot be parsed`() {
        assertThat(toLocalTime("not a time")).isEqualTo("not a time")
    }

    @Test
    fun `sorts entries newest first by timestamp even when files disagree`() {
        tempFolder.newFile("simprints.0.log").apply {
            writeText("2026-10-26T14:10:59.100Z Info: (A) early\n2026-10-26T14:10:59.900Z Info: (A) late\n")
            setLastModified(5_000L)
        }
        tempFolder.newFile("simprints.1.log").apply {
            writeText("2026-10-26T14:10:59.500Z Info: (A) middle\n")
            setLastModified(1_000L)
        }

        assertThat(readRecentLogEntries(tempFolder.root, 10).map { it.message })
            .containsExactly("(A) late", "(A) middle", "(A) early")
            .inOrder()
    }

    @Test
    fun `reads short fractions as milliseconds when sorting`() {
        // ".16Z" is 160 ms, which is later than ".1Z" (100 ms), even though it sorts differently as text
        tempFolder.newFile("simprints.0.log").writeText(
            "2026-10-26T14:10:59.1Z Info: (A) one-tenth\n" +
                "2026-10-26T14:10:59.16Z Info: (A) sixteen\n",
        )

        assertThat(readRecentLogEntries(tempFolder.root, 10).map { it.message })
            .containsExactly("(A) sixteen", "(A) one-tenth")
            .inOrder()
    }

    @Test
    fun `pads short fractions when converting to local time`() {
        assertThat(toLocalTime("2026-10-26T14:10:59.16Z", TimeZone.getTimeZone("UTC")))
            .isEqualTo("14:10:59.160")
    }

    @Test
    fun `includes the date when converting to local date and time`() {
        assertThat(toLocalDateTime("2026-10-26T14:10:59.729Z", TimeZone.getTimeZone("Asia/Tokyo")))
            .isEqualTo("2026-10-26 23:10:59.729")
    }

    private fun entry(
        severity: String,
        message: String,
    ) = AppLogEntry(
        timestamp = "2026-10-26T14:10:59.100Z",
        severity = severity,
        message = message,
        lines = listOf("2026-10-26T14:10:59.100Z $severity: $message"),
    )
}
