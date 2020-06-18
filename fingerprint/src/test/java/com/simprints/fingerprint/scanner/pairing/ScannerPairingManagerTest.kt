package com.simprints.fingerprint.scanner.pairing

import com.google.common.truth.Truth.assertThat
import com.simprints.testtools.common.syntax.assertThrows
import io.mockk.mockk
import org.junit.Test

class ScannerPairingManagerTest {

    private val scannerPairingManager = ScannerPairingManager(mockk())

    @Test
    fun interpretEnteredTextAsSerialNumber_worksCorrectlyForValidStrings() {
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("987654")).isEqualTo("SP987654")
        assertThat(scannerPairingManager.interpretEnteredTextAsSerialNumber("012345")).isEqualTo("SP012345")
    }

    @Test
    fun interpretEnteredTextAsSerialNumber_throwsForInvalidStrings() {
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("12345") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("9876543") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("-12345") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("1234o2") }
        assertThrows<NumberFormatException> { scannerPairingManager.interpretEnteredTextAsSerialNumber("1874.5") }
    }
}
