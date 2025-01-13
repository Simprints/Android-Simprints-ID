package com.simprints.fingerprint.infra.scannermock.simulated.v2

import com.simprints.fingerprint.infra.scanner.v2.domain.IncomingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.OutgoingMessage
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprint.infra.scanner.v2.domain.main.message.vero.models.DigitalValue
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.common.SimulatedScanner
import com.simprints.fingerprint.infra.scannermock.simulated.v2.response.SimulatedResponseHelperV2
import com.simprints.fingerprint.infra.scannermock.simulated.v2.response.SimulatedRootResponseHelper
import com.simprints.fingerprint.infra.scannermock.simulated.v2.response.SimulatedUn20ResponseHelper
import com.simprints.fingerprint.infra.scannermock.simulated.v2.response.SimulatedVeroResponseHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.OutputStream

class SimulatedScannerV2(
    simulatedScannerManager: SimulatedScannerManager,
    val scannerState: SimulatedScannerStateV2 = SimulatedScannerStateV2(),
) : SimulatedScanner(simulatedScannerManager) {
    private lateinit var returnStream: OutputStream
    private val dispatcher = Dispatchers.IO
    private val scope = CoroutineScope(dispatcher)
    private val simulatedCommandInputStream = SimulatedCommandInputStream()
    private val simulatedResponseOutputStream = SimulatedResponseOutputStream()

    @Suppress("unused")
    private val simulatedRootResponseHelper = SimulatedRootResponseHelper(simulatedScannerManager, this)
        .apply { respondToCommands(simulatedCommandInputStream.rootCommands) }

    @Suppress("unused")
    private val simulatedVeroResponseHelper = SimulatedVeroResponseHelper(simulatedScannerManager, this)
        .apply { respondToCommands(simulatedCommandInputStream.veroCommands) }

    @Suppress("unused")
    private val simulatedUn20ResponseHelper = SimulatedUn20ResponseHelper(simulatedScannerManager)
        .apply { respondToCommands(simulatedCommandInputStream.un20Commands) }

    override fun handleAppToScannerEvent(
        bytes: ByteArray,
        returnStream: OutputStream,
    ) {
        this.returnStream = returnStream
        simulatedCommandInputStream.updateWithNewBytes(bytes, scannerState.mode)
    }

    private fun <T : OutgoingMessage, R : IncomingMessage> SimulatedResponseHelperV2<T, R>.respondToCommands(commands: Flow<T>) {
        scope.launch {
            commands.collect { command ->
                scannerState.updateStateAccordingToOutgoingMessage(command)
                val response = createResponseToCommand(command)
                val bytes = simulatedResponseOutputStream.serialize(response)
                bytes.forEach { writeResponseToStream(it, returnStream) }
                resolveEventQueue()
            }
        }
    }

    private suspend fun resolveEventQueue() {
        scannerState.eventQueue.forEach {
            // add 300 ms delay to let the previous event to be processed before the next one
            delay(300)
            it.invoke(this)
        }
        scannerState.eventQueue.clear()
    }

    fun triggerUn20StateChangeEvent(digitalValue: DigitalValue) {
        val event = Un20StateChangeEvent(digitalValue)
        val bytes = simulatedResponseOutputStream.serialize(event)
        bytes.forEach { writeResponseToStream(it, returnStream) }
    }

    override fun disconnect() {
        simulatedCommandInputStream.disconnect()
    }
}
