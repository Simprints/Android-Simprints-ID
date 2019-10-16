package com.simprints.fingerprintscannermock.simulated.v2

import android.annotation.SuppressLint
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.ScannerState
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import java.io.OutputStream

class SimulatedScannerV2(simulatedScannerManager: SimulatedScannerManager,
                         scannerState: ScannerState = ScannerState())
    : SimulatedScanner(scannerState) {

    private lateinit var returnStream: OutputStream

    private val simulatedCommandInputStream = SimulatedCommandInputStream()

    @Suppress("unused")
    private val simulatedVeroResponseHelper = SimulatedVeroResponseHelper(simulatedScannerManager, this)
        .apply { respondToCommands(simulatedCommandInputStream.veroCommands) }

    @Suppress("unused")
    private val simulatedUn20ResponseHelper = SimulatedUn20ResponseHelper(simulatedScannerManager, this)
        .apply { respondToCommands(simulatedCommandInputStream.un20Commands) }

    override fun handleAppToScannerEvent(bytes: ByteArray, returnStream: OutputStream) {
        this.returnStream = returnStream
        simulatedCommandInputStream.updateWithNewBytes(bytes)
    }

    @SuppressLint("CheckResult")
    private fun <T : OutgoingMessage> SimulatedResponseHelperV2<T>.respondToCommands(commands: Flowable<T>) {
        commands.subscribeBy(onNext = { command ->
            scannerState.updateStateAccordingToOutgoingMessage(command)
            val response = this.createResponseToCommand(command)
            writeResponseToStream(response, returnStream)
        })
    }
}
