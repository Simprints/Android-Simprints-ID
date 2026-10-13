package com.simprints.feature.externalcredential.screens.scandocument.usecase

import com.google.common.truth.Truth.assertThat
import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.BACDeniedException
import org.jmrtd.BACKey
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

internal class ChipReadLogTest {
    private val log = ChipReadLog(sensitiveValues = listOf(DOCUMENT_NUMBER, DATE_OF_BIRTH, DATE_OF_EXPIRY, "<"))

    @Test
    fun `adds plain status messages`() {
        log.add("Connected to the chip")

        assertThat(log.lines).containsExactly("Connected to the chip")
    }

    @Test
    fun `successful step does not add anything and returns the result`() {
        val result = log.step("Reading DG1") { "result" }

        assertThat(result).isEqualTo("result")
        assertThat(log.lines).isEmpty()
    }

    @Test
    fun `failed step logs the status word as the reason and rethrows`() {
        val exception = CardServiceException("Wrapped", IOException("Transceive failed"), 0x6982)

        val thrown = assertThrows(CardServiceException::class.java) { log.step("Reading EF.SOD") { throw exception } }

        assertThat(thrown).isSameInstanceAs(exception)
        assertThat(log.lines).containsExactly("Reading EF.SOD failed: access not granted (SW 6982)")
    }

    @Test
    fun `failure without status word logs the root cause`() {
        runCatching { log.step("Connection") { throw IOException("Wrapped", IllegalStateException("Tag was lost.")) } }

        assertThat(log.lines).containsExactly("Connection failed: IllegalStateException: Tag was lost.")
    }

    @Test
    fun `failure already logged by a step is not logged again`() {
        val exception = IllegalStateException("Boom")
        runCatching { log.step("BAC") { throw exception } }

        log.addFailure("Read", exception)

        assertThat(log.lines).containsExactly("BAC failed: IllegalStateException: Boom")
    }

    @Test
    fun `access key values are redacted from all entries`() {
        val accessKey = BACKey(DOCUMENT_NUMBER, DATE_OF_BIRTH, DATE_OF_EXPIRY)
        val exception = IOException("Denied for $DOCUMENT_NUMBER $DATE_OF_BIRTH $DATE_OF_EXPIRY ($accessKey)")

        log.add("Key ${DOCUMENT_NUMBER.lowercase()}")
        runCatching { log.step("BAC") { throw exception } }

        val text = log.lines.joinToString("\n")
        assertThat(text).doesNotContain(DOCUMENT_NUMBER)
        assertThat(text).doesNotContain(DOCUMENT_NUMBER.lowercase())
        assertThat(text).doesNotContain(DATE_OF_BIRTH)
        assertThat(text).doesNotContain(DATE_OF_EXPIRY)
        assertThat(text).contains(ChipReadLog.REDACTED)
    }

    @Test
    fun `wrong access key is reported from the BAC status word`() {
        val accessKey = BACKey(DOCUMENT_NUMBER, DATE_OF_BIRTH, DATE_OF_EXPIRY)
        runCatching { log.step("BAC") { throw BACDeniedException("Mutual authentication failed", accessKey, 0x6300) } }

        assertThat(log.lines).containsExactly("BAC failed: wrong access key (SW 6300)")
    }

    @Test
    fun `short values are not redacted`() {
        log.add("<<<")

        assertThat(log.lines).containsExactly("<<<")
    }

    @Test
    fun `unknown status words are described as chip error`() {
        assertThat(ChipReadLog.describeStatusWord(0x1234)).isEqualTo("chip error")
    }

    companion object {
        private const val DOCUMENT_NUMBER = "L898902C3"
        private const val DATE_OF_BIRTH = "740812"
        private const val DATE_OF_EXPIRY = "120415"
    }
}
