package com.simprints.fingerprint.scanner.wrapper

import com.simprints.fingerprint.infra.scanner.v2.tools.mapPotentialErrorFromScanner
import com.simprints.fingerprint.scanner.controllers.v2.CypressOtaHelper
import com.simprints.fingerprint.scanner.controllers.v2.StmOtaHelper
import com.simprints.fingerprint.scanner.controllers.v2.Un20OtaHelper
import com.simprints.fingerprint.scanner.domain.ota.CypressOtaStep
import com.simprints.fingerprint.scanner.domain.ota.StmOtaStep
import com.simprints.fingerprint.scanner.domain.ota.Un20OtaStep
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import com.simprints.fingerprint.infra.scanner.v2.scanner.Scanner as ScannerV2

class ScannerOtaOperationsWrapper(
    private val macAddress: String,
    private val scannerV2: ScannerV2,
    private val cypressOtaHelper: CypressOtaHelper,
    private val stmOtaHelper: StmOtaHelper,
    private val un20OtaHelper: Un20OtaHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {

    fun performCypressOta(firmwareVersion: String): Flow<CypressOtaStep> =
        cypressOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)

    fun performStmOta(firmwareVersion: String): Flow<StmOtaStep> =
        stmOtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)

    fun performUn20Ota(firmwareVersion: String): Flow<Un20OtaStep> =
        un20OtaHelper.performOtaSteps(scannerV2, macAddress, firmwareVersion)
            .mapPotentialErrorFromScanner()
            .flowOn(ioDispatcher)
}
