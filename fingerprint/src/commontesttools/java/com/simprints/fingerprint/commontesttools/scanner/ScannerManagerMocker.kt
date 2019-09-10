package com.simprints.fingerprint.commontesttools.scanner

import com.simprints.fingerprint.controllers.scanner.ScannerManager
import com.simprints.fingerprintscanner.v1.Scanner
import com.simprints.testtools.common.syntax.whenThis

fun ScannerManager.setupScannerManagerMockWithMockedScanner(mockedScanner: Scanner = createMockedScanner()) {
    whenThis { scanner } thenReturn mockedScanner
}
