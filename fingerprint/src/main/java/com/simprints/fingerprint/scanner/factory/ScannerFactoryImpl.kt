package com.simprints.fingerprint.scanner.factory

import com.simprints.fingerprint.scanner.ui.ScannerUiHelper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapper
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV1
import com.simprints.fingerprint.scanner.wrapper.ScannerWrapperV2
import com.simprints.fingerprintscanner.component.bluetooth.BluetoothComponentAdapter
import com.simprints.fingerprintscanner.v2.domain.main.packet.Channel
import com.simprints.fingerprintscanner.v2.incoming.main.MessageInputStream
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.Un20ResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroEventAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.accumulators.VeroResponseAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.Un20ResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroEventParser
import com.simprints.fingerprintscanner.v2.incoming.main.message.parsers.VeroResponseParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.ByteArrayToPacketAccumulator
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketParser
import com.simprints.fingerprintscanner.v2.incoming.main.packet.PacketRouter
import com.simprints.fingerprintscanner.v2.outgoing.main.MessageOutputStream
import com.simprints.fingerprintscanner.v2.outgoing.main.message.MessageSerializer
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketDispatcher
import com.simprints.fingerprintscanner.v2.outgoing.main.packet.PacketSerializer
import com.simprints.fingerprintscanner.v2.tools.lang.objects
import com.simprints.fingerprintscanner.v1.Scanner as ScannerV1
import com.simprints.fingerprintscanner.v2.scanner.Scanner as ScannerV2

class ScannerFactoryImpl(private val bluetoothAdapter: BluetoothComponentAdapter,
                         private val scannerUiHelper: ScannerUiHelper) : ScannerFactory {

    override fun create(macAddress: String): ScannerWrapper {
        // TODO : Determine whether to create a ScannerV1 or a ScannerV2
        return createScannerV1(macAddress)
    }

    fun createScannerV1(macAddress: String): ScannerWrapper =
        ScannerWrapperV1(
            ScannerV1(macAddress, bluetoothAdapter)
        )

    fun createScannerV2(macAddress: String): ScannerWrapper =
        ScannerWrapperV2(
            ScannerV2(
                MessageInputStream(
                    PacketRouter(
                        Channel.Remote::class.objects(),
                        { source },
                        ByteArrayToPacketAccumulator(PacketParser())
                    ),
                    VeroResponseAccumulator(VeroResponseParser()),
                    VeroEventAccumulator(VeroEventParser()),
                    Un20ResponseAccumulator(Un20ResponseParser())
                ),
                MessageOutputStream(
                    MessageSerializer(PacketParser()),
                    PacketDispatcher(PacketSerializer())
                )
            ),
            scannerUiHelper,
            macAddress,
            bluetoothAdapter
        )
}
