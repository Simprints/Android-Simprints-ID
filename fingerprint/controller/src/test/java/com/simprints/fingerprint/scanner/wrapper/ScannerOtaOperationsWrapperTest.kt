package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
import com.simprints.fingerprint.scanner.controllers.v2.CypressOtaHelper
import com.simprints.fingerprint.scanner.controllers.v2.StmOtaHelper
import com.simprints.fingerprint.scanner.controllers.v2.Un20OtaHelper
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

internal class ScannerOtaOperationsWrapperTest {

    private lateinit var scannerWrapper: ScannerOtaOperationsWrapper
    @MockK
    private lateinit var cypressOtaHelper :CypressOtaHelper
    @MockK
    private lateinit var stmOtaHelper :StmOtaHelper
    @MockK
    private lateinit var un20OtaHelper :Un20OtaHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        scannerWrapper = ScannerOtaOperationsWrapper(
            "",
            mockk(),
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper,
            UnconfinedTestDispatcher()
        )
    }

    @Test(expected = OtaFailedException::class)
    fun `should throw OtaFailedException if performOtaSteps throws `(): Unit = runTest {
        every { cypressOtaHelper.performOtaSteps(any(), any(), any()) } throws OtaFailedException()
        scannerWrapper.performCypressOta("")
    }
    @Test
    fun `should not throw OtaFailedException if performOtaSteps does not throw `(): Unit = runTest {
        every { stmOtaHelper.performOtaSteps(any(), any(), any()) } returns mockk()
        scannerWrapper.performStmOta("")
    }

}
