package com.simprints.fingerprint.controllers.core.preferencesManager

import com.google.common.truth.Truth
import com.simprints.fingerprint.scanner.data.FirmwareTestData
import com.simprints.fingerprint.scanner.domain.versions.ScannerHardwareRevisionsSerializer
import com.simprints.id.data.prefs.IdPreferencesManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Test

class FingerprintPreferencesManagerImplTest {

    @RelaxedMockK
    lateinit var prefs: IdPreferencesManager

    @MockK
    lateinit var scannerHardwareRevisionsSerializer: ScannerHardwareRevisionsSerializer

    lateinit var fingerprintPreferencesManager: FingerprintPreferencesManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        fingerprintPreferencesManager =
            FingerprintPreferencesManagerImpl(prefs, scannerHardwareRevisionsSerializer)
    }

    @Test
    fun getScannerHardwareRevisions() {
        // Given
        every {
            scannerHardwareRevisionsSerializer.build(any())
        } returns FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP
        // When
        val scannerHardwareRevisions = fingerprintPreferencesManager.scannerHardwareRevisions
        // Then
        Truth.assertThat(scannerHardwareRevisions)
            .isEqualTo(FirmwareTestData.RESPONSE_HARDWARE_REVISIONS_MAP)
    }
}
