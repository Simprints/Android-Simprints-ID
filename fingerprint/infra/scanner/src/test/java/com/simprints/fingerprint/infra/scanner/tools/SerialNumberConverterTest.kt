package com.simprints.fingerprint.infra.scanner.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SerialNumberConverterTest {
    private val serialNumberConverter = SerialNumberConverter()

    @Test
    fun convertMacToSerial_worksForSerialBelowLimit() {
        assertThat(serialNumberConverter.convertMacAddressToSerialNumber("F0:AC:D7:C0:01:00"))
            .isEqualTo("SP000256")
    }

    @Test
    fun convertMacToSerial_worksForSerialAboveLimit() {
        assertThat(serialNumberConverter.convertMacAddressToSerialNumber("F0:AC:D7:CF:44:CC"))
            .isEqualTo("SP000652")
    }

    @Test
    fun convertSerialToMac_worksForSerialsNotOnList() {
        assertThat(serialNumberConverter.convertSerialNumberToMacAddress("SP000256"))
            .isEqualTo("F0:AC:D7:C0:01:00")
    }

    @Test
    fun convertSerialToMac_worksForSerialsOnList() {
        assertThat(serialNumberConverter.convertSerialNumberToMacAddress("SP000652"))
            .isEqualTo("F0:AC:D7:CF:44:CC")
    }
}
