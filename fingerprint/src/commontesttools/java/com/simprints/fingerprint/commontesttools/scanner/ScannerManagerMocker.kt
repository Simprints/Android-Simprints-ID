package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.testtools.common.syntax.whenThis

fun ScannerManager.setupScannerManagerMockWithMockedScanner(mockedScanner: ScannerWrapper = ScannerWrapperV1(createMockedScannerV1())) {
    whenThis { scanner } thenReturn mockedScanner
}
