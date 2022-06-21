package com.simprints.fingerprint.scanner.wrapper


import com.google.common.truth.Truth
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class ScannerWrapperV2Test {

    private lateinit var scannerWrapper: ScannerWrapperV2


    @Before
    fun setUp() {
         scannerWrapper = ScannerWrapperV2(
            mockk(), mockk(), "", mockk(), mockk(), mockk(),
            mockk(), mockk()
        )
    }

    @Test
    fun `test imageTransfer should be supported in v2 scanners`() {
        Truth.assertThat(scannerWrapper.isImageTransferSupported()).isTrue()
    }

}
