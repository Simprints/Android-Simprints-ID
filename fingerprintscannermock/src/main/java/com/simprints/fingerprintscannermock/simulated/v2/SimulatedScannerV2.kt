package com.simprints.fingerprintscannermock.simulated.v2

import android.annotation.SuppressLint
import com.simprints.fingerprintscanner.v2.domain.message.IncomingMessage
import com.simprints.fingerprintscanner.v2.domain.message.OutgoingMessage
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.TriggerButtonPressedEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.events.Un20StateChangeEvent
import com.simprints.fingerprintscanner.v2.domain.message.vero.models.DigitalValue
import com.simprints.fingerprintscannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprintscannermock.simulated.common.ScannerState
import com.simprints.fingerprintscannermock.simulated.common.SimulatedScanner
import io.reactivex.Flowable
import io.reactivex.rxkotlin.subscribeBy
import java.io.OutputStream

class SimulatedScannerV2(simulatedScannerManager: SimulatedScannerManager,
                         scannerState: ScannerState = ScannerState())
    : SimulatedScanner(simulatedScannerManager, scannerState) {

    private lateinit var returnStream: OutputStream

    private val simulatedCommandInputStream = SimulatedCommandInputStream()
    private val simulatedResponseOutputStream = SimulatedResponseOutputStream()

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
    private fun <T : OutgoingMessage, R : IncomingMessage> SimulatedResponseHelperV2<T, R>.respondToCommands(commands: Flowable<T>) {
        commands.subscribeBy(onNext = { command ->
            scannerState.updateStateAccordingToOutgoingMessage(command)
            val response = this.createResponseToCommand(command)
            val bytes = simulatedResponseOutputStream.serialize(response)
            bytes.forEach { writeResponseToStream(it, returnStream) }
            resolveEventQueue()
        })
    }

    private fun resolveEventQueue() {
        scannerState.eventQueue.forEach { it.invoke(this) }
        scannerState.eventQueue.clear()
    }

    fun triggerUn20StateChangeEvent(digitalValue: DigitalValue) {
        val event = Un20StateChangeEvent(digitalValue)
        val bytes = simulatedResponseOutputStream.serialize(event)
        bytes.forEach { writeResponseToStream(it, returnStream) }
    }

    fun triggerButtonPressedEvent() {
        val event = TriggerButtonPressedEvent()
        val bytes = simulatedResponseOutputStream.serialize(event)
        bytes.forEach { writeResponseToStream(it, returnStream) }
    }
}
