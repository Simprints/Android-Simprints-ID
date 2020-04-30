package com.simprints.fingerprint.scanner.factory

import com.simprints.fingerprint.controllers.core.crashreport.FingerprintCrashReportManager
import com.simprints.fingerprint.controllers.core.preferencesManager.FingerprintPreferencesManager
import com.simprints.fingerprint.scanner.domain.ScannerGeneration
import com.simprints.fingerprint.scanner.tools.ScannerGenerationDeterminer
import com.simprints.fingerprint.scanner.tools.SerialNumberConverter
import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV2
import com.simprints.fingerprintscanner.component.bluetooth.ComponentBluetoothAdapter
import com.simprints.fingerprintscanner.v2.channel.CypressOtaMessageChannel
import com.simprints.fingerprintscanner.v2.channel.MainMessageChannel
import com.simprints.fingerprintscanner.v2.channel.RootMessageChannel
import com.simprints.fingerprintscanner.v2.channel.StmOtaMessageChannel
import com.simprints.fingerprintscanner.v2.domain.main.packet.Route
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.cypressota.CypressOtaResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.MainMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.incoming.root.RootMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.root.RootResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.root.RootResponseParser
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaMessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.stmota.StmOtaResponseParser
import com.simprints.fingerprintscanner.v2.outgoing.common.OutputStreamDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.cypressota.CypressOtaMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.MainMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.root.RootMessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.stmota.StmOtaMessageSerializer
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandler
import com.simprints.fingerprintscanner.v2.scanner.errorhandler.ResponseErrorHandlingStrategy
import com.simprints.fingerprintscanner.v2.scanner.ota.cypress.CypressOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.stm.StmOtaController
import com.simprints.fingerprintscanner.v2.scanner.ota.un20.Un20OtaController
import com.simprints.fingerprintscanner.v2.tools.crc.Crc32Calculator
import timber.log.Timber
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerFactoryImpl(private val bluetoothAdapter: ComponentBluetoothAdapter,
                         private val preferencesManager: FingerprintPreferencesManager,
                         private val crashReportManager: FingerprintCrashReportManager,
                         private val scannerUiHelper: ScannerUiHelper,
                         private val serialNumberConverter: SerialNumberConverter,
                         private val scannerGenerationDeterminer: ScannerGenerationDeterminer) : ScannerFactory {

    override fun create(macAddress: String): ScannerWrapper {
        val availableScannerGenerations = preferencesManager.scannerGenerations

        val scannerGenerationToUse = when (availableScannerGenerations.size) {
            1 -> availableScannerGenerations.single()
            else -> scannerGenerationDeterminer.determineScannerGenerationFromSerialNumber(
                serialNumberConverter.convertMacAddressToSerialNumber(macAddress)
            )
        }.also {
            Timber.i("Using scanner generation $it")
        }

        return when (scannerGenerationToUse) {
            ScannerGeneration.VERO_1 -> createScannerV1(macAddress)
            ScannerGeneration.VERO_2 -> createScannerV2(macAddress)
        }
    }

    fun createScannerV1(macAddress: String): ScannerWrapper =
        ScannerWrapperV1(
            ScannerV1(macAddress, bluetoothAdapter)
        )

    fun createScannerV2(macAddress: String): ScannerWrapper =
        ScannerWrapperV2(
            ScannerV2(
                MainMessageChannel(
                    MainMessageInputStream(
                        PacketRouter(
                            listOf(Route.Remote.VeroServer, Route.Remote.VeroEvent, Route.Remote.Un20Server),
                            { source },
                            ByteArrayToPacketAccumulator(PacketParser())
                        ),
                        VeroResponseAccumulator(VeroResponseParser()),
                        VeroEventAccumulator(VeroEventParser()),
                        Un20ResponseAccumulator(Un20ResponseParser())
                    ),
                    MainMessageOutputStream(
                        MainMessageSerializer(),
                        OutputStreamDispatcher()
                    )
                ),
                RootMessageChannel(
                    RootMessageInputStream(
                        RootResponseAccumulator(RootResponseParser())
                    ),
                    RootMessageOutputStream(
                        RootMessageSerializer(),
                        OutputStreamDispatcher()
                    )
                ),
                CypressOtaMessageChannel(
                    CypressOtaMessageInputStream(
                        CypressOtaResponseParser()
                    ),
                    CypressOtaMessageOutputStream(
                        CypressOtaMessageSerializer(),
                        OutputStreamDispatcher()
                    )
                ),
                StmOtaMessageChannel(
                    StmOtaMessageInputStream(
                        StmOtaResponseParser()
                    ),
                    StmOtaMessageOutputStream(
                        StmOtaMessageSerializer(),
                        OutputStreamDispatcher()
                    )
                ),
                CypressOtaController(Crc32Calculator()),
                StmOtaController(),
                Un20OtaController(Crc32Calculator()),
                ResponseErrorHandler(ResponseErrorHandlingStrategy.DEFAULT)
            ),
            scannerUiHelper,
            macAddress,
            bluetoothAdapter,
            crashReportManager
        )
}
