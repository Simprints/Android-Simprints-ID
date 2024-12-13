package com.simprints.fingerprint.infra.scanner

import com.google.common.truth.Truth.assertThat
import com.simprints.fingerprint.infra.scanner.nfc.ComponentMifareUltralight
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcAdapter
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class NfcManagerTest {
    @MockK
    private lateinit var nfcAdapter: ComponentNfcAdapter

    private lateinit var manager: NfcManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)

        manager = NfcManager(nfcAdapter)
    }

    @Test
    fun `Correctly returns if no nfc capability`() {
        every { nfcAdapter.isNull() } returns true
        assertThat(manager.doesDeviceHaveNfcCapability()).isFalse()
    }

    @Test
    fun `Correctly returns if has nfc capability`() {
        every { nfcAdapter.isNull() } returns false
        assertThat(manager.doesDeviceHaveNfcCapability()).isTrue()
    }

    @Test
    fun `Correctly returns disabled if no nfc capability`() {
        every { nfcAdapter.isNull() } returns true
        assertThat(manager.isNfcEnabled()).isFalse()
    }

    @Test
    fun `Correctly returns disabled if nfc disabled`() {
        every { nfcAdapter.isNull() } returns true
        every { nfcAdapter.isEnabled() } returns false
        assertThat(manager.isNfcEnabled()).isFalse()
    }

    @Test
    fun `Correctly returns enabled if nfc enabled`() {
        every { nfcAdapter.isNull() } returns false
        every { nfcAdapter.isEnabled() } returns true
        assertThat(manager.isNfcEnabled()).isTrue()
    }

    @Test
    fun `Does not enable reader if no nfc capability`() = runTest {
        every { nfcAdapter.isNull() } returns true
        manager.enableReaderMode(mockk())
        coVerify(exactly = 0) { nfcAdapter.enableReaderMode(any(), any(), any(), any()) }
    }

    @Test
    fun `Enables reader when called`() = runTest {
        every { nfcAdapter.isNull() } returns false
        manager.enableReaderMode(mockk())
        coVerify { nfcAdapter.enableReaderMode(any(), any(), any(), any()) }
    }

    @Test
    fun `Does not disable reader if no nfc capability`() = runTest {
        every { nfcAdapter.isNull() } returns true
        manager.disableReaderMode(mockk())
        coVerify(exactly = 0) { nfcAdapter.disableReaderMode(any()) }
    }

    @Test
    fun `Disables reader when called`() = runTest {
        every { nfcAdapter.isNull() } returns false
        manager.disableReaderMode(mockk())
        coVerify(exactly = 1) { nfcAdapter.disableReaderMode(any()) }
    }

    @Test
    fun `Throws when interpreting null tag`() = runTest {
        every { nfcAdapter.getMifareUltralight(any()) } returns null

        assertThrows<IllegalArgumentException> {
            manager.readMacAddressDataFromBluetoothEasyPairTag(null)
        }
    }

    @Test
    fun `Throws when invalid tag`() = runTest {
        val tag = mockk<ComponentMifareUltralight>(relaxed = true)
        every { tag.readPages(any()) } returns "".toByteArray()
        every { nfcAdapter.getMifareUltralight(any()) } returns tag

        assertThrows<IllegalArgumentException> {
            manager.readMacAddressDataFromBluetoothEasyPairTag(null)
        }
    }

    @Test
    fun `Successfully parses the tag`() = runTest {
        val tag = mockk<ComponentMifareUltralight>(relaxed = true)
        every { tag.readPages(any()) } returns byteArrayOf(6, 5, 4, 3, 2, 1)
        every { nfcAdapter.getMifareUltralight(any()) } returns tag

        val result = manager.readMacAddressDataFromBluetoothEasyPairTag(null)
        assertThat(result).isEqualTo("01:02:03:04:05:06")
    }
}
