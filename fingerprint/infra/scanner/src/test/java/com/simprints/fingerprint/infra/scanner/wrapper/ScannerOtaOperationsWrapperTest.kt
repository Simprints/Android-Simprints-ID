package com.simprints.fingerprint.infra.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.helpers.CypressOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.StmOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.Un20OtaHelper
import com.simprints.fingerprint.infra.scanner.v2.exceptions.ota.OtaFailedException
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
    private lateinit var cypressOtaHelper: CypressOtaHelper

    @MockK
    private lateinit var stmOtaHelper: StmOtaHelper

    @MockK
    private lateinit var un20OtaHelper: Un20OtaHelper

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        scannerWrapper = ScannerOtaOperationsWrapper(
            "",
            mockk(),
            cypressOtaHelper,
            stmOtaHelper,
            un20OtaHelper,
            UnconfinedTestDispatcher(),
        )
    }

    @Test(expected = OtaFailedException::class)
    fun `should throw OtaFailedException if cypress performOtaSteps throws `(): Unit = runTest {
        every { cypressOtaHelper.performOtaSteps(any(), any(), any()) } throws OtaFailedException()
        scannerWrapper.performCypressOta("")
    }

    @Test
    fun `should not throw OtaFailedException if cypress performOtaSteps does not throw `(): Unit = runTest {
        every { cypressOtaHelper.performOtaSteps(any(), any(), any()) } returns mockk()
        scannerWrapper.performCypressOta("")
    }

    @Test(expected = OtaFailedException::class)
    fun `should throw OtaFailedException if stm performOtaSteps throws `(): Unit = runTest {
        every { stmOtaHelper.performOtaSteps(any(), any(), any()) } throws OtaFailedException()
        scannerWrapper.performStmOta("")
    }

    @Test
    fun `should not throw OtaFailedException if stm performOtaSteps does not throw `(): Unit = runTest {
        every { stmOtaHelper.performOtaSteps(any(), any(), any()) } returns mockk()
        scannerWrapper.performStmOta("")
    }

    @Test(expected = OtaFailedException::class)
    fun `should throw OtaFailedException if un20 performOtaSteps throws `(): Unit = runTest {
        every { un20OtaHelper.performOtaSteps(any(), any(), any()) } throws OtaFailedException()
        scannerWrapper.performUn20Ota("")
    }

    @Test
    fun `should not throw OtaFailedException if un20 performOtaSteps does not throw `(): Unit = runTest {
        every { un20OtaHelper.performOtaSteps(any(), any(), any()) } returns mockk()
        scannerWrapper.performUn20Ota("")
    }
}
