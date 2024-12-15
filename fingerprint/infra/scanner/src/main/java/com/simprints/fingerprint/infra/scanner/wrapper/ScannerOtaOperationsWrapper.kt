package com.simprints.fingerprint.infra.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.infra.scanner.domain.ota.Un20OtaStep
import com.simprints.fingerprint.infra.scanner.helpers.CypressOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.StmOtaHelper
import com.simprints.fingerprint.infra.scanner.helpers.Un20OtaHelper
import com.simprints.fingerprint.infra.scanner.v2.tools.mapPotentialErrorFromScanner
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

class ScannerOtaOperationsWrapper internal constructor(
    private val macAddress: String,
    private val scannerV2: ScannerV2,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    fun performCypressOta(firmwareVersion: String): Flow<CypressOtaStep> = cypressOtaHelper
        .performOtaSteps(scannerV2, macAddress, firmwareVersion)
        .mapPotentialErrorFromScanner()
        .flowOn(ioDispatcher)

    fun performStmOta(firmwareVersion: String): Flow<StmOtaStep> = stmOtaHelper
        .performOtaSteps(scannerV2, macAddress, firmwareVersion)
        .mapPotentialErrorFromScanner()
        .flowOn(ioDispatcher)

    fun performUn20Ota(firmwareVersion: String): Flow<Un20OtaStep> = un20OtaHelper
        .performOtaSteps(scannerV2, macAddress, firmwareVersion)
        .mapPotentialErrorFromScanner()
        .flowOn(ioDispatcher)
}
