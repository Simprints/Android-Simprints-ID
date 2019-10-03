package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.scanner.ScannerManager
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.testtools.common.syntax.whenThis

fun ScannerManager.setupScannerManagerMockWithMockedScanner(mockedScanner: ScannerWrapper) {
    whenThis { scanner } thenReturn mockedScanner
}
