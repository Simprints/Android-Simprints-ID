package com.simprints.fingerprint.scanner.domain.versions

import com.google.common.truth.Truth
import com.simprints.core.tools.json.JsonHelper
import com.simprints.fingerprint.scanner.data.FirmwareTestData
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class ScannerHardwareRevisionsSerializerTest {

    lateinit var serializer: ScannerHardwareRevisionsSerializer

    @MockK
    lateinit var jsonHelper: JsonHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        serializer = ScannerHardwareRevisionsSerializer(jsonHelper)
    }

    @Test
    fun `test build ScannerHardwareRevisions success`() {
        //Given
        every { jsonHelper.fromJson<ScannerHardwareRevisions>(any()) } returns FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        val jsonString =
            "{\"E-1\":{\"cypress\":\"1.E-1.1\",\"stm\":\"1.E-1.1\",\"un20\":\"1.E-1.0\"}}"
        // When
        val scannerHardwareRevisions = serializer.build(jsonString)
        // Then
        Truth.assertThat(scannerHardwareRevisions)
            .isEqualTo(FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP)
        Truth.assertThat(scannerHardwareRevisions["E-1"]).isNotNull()

    }

    @Test
    fun `test build ScannerHardwareRevisions failure with empty string`() {
        //Given
        every { jsonHelper.fromJson<ScannerHardwareRevisions>(any()) } throws Exception("Malformed json")
        val jsonString = ""
        // When
        val scannerHardwareRevisions = serializer.build(jsonString)
        // Then
        Truth.assertThat(scannerHardwareRevisions.isEmpty()).isEqualTo(true)
    }

    @Test
    fun `test build ScannerHardwareRevisions failure with invalid JSON`() {
        //Given
        every { jsonHelper.fromJson<ScannerHardwareRevisions>(any()) } throws Exception("Malformed json")
        val jsonString = "I am an invalid JSON"
        // When
        val scannerHardwareRevisions = serializer.build(jsonString)
        // Then
        Truth.assertThat(scannerHardwareRevisions.isEmpty()).isEqualTo(true)
    }
}
