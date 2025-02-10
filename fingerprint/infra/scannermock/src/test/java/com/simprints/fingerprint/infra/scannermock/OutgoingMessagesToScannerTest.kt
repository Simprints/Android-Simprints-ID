package com.simprints.fingerprint.infra.scannermock

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.simprints.fingerprint.infra.scanner.v1.Scanner
import com.simprints.fingerprint.infra.scanner.v1.ScannerCallback
import com.simprints.fingerprint.infra.scanner.v1.wrappedScannerCallback
import com.simprints.fingerprint.infra.scannermock.simulated.SimulatedScannerManager
import com.simprints.fingerprint.infra.scannermock.simulated.SimulationMode
import com.simprints.fingerprint.infra.scannermock.simulated.component.SimulatedBluetoothAdapter
import com.simprints.fingerprint.infra.scannermock.simulated.tools.byteArrayFromHexString
import io.mockk.mockk
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.LinkedBlockingQueue

@RunWith(AndroidJUnit4::class)
class OutgoingMessagesToScannerTest {
    private lateinit var testScanner: Scanner
    private lateinit var receivedValues: MutableList<ByteArray>
    private var testObserver: (message: ByteArray) -> Unit = { bytes ->
        receivedValues.add(bytes)
    }

    @Before
    fun setup() {
        val simulatedScannerManager = SimulatedScannerManager(
            SimulationMode.V1,
            outgoingStreamObservers = setOf(testObserver),
            context = mockk(),
        )
        receivedValues = mutableListOf()
        val simulatedBluetoothAdapter = SimulatedBluetoothAdapter(simulatedScannerManager)
        testScanner = Scanner("F0:AC:D7:C0:00:00", simulatedBluetoothAdapter)

        assertOnSuccessCallback { testScanner.connect(it) }
    }

    @Test
    fun outgoing_message_un20wakeup() {
        assertOnSuccessCallback { testScanner.un20Wakeup(it) }

        assertOutgoingMessageIs(WAKE_UP_MESSAGE)
        assertOutgoingMessageIs(SENSOR_INFO_REQUEST, 1)
    }

    @Test
    fun outgoing_message_resetUI() {
        assertOnSuccessCallback { testScanner.resetUI(it) }

        assertOutgoingMessageIs(RESET_UI_COMMAND)
    }

    @Test
    fun outgoing_message_internal_getImageQuality() {
        invokePrivateMethod(testScanner, "internal_getImageQuality")

        assertOutgoingMessageIs(GET_IMAGE_QUALITY)
    }

    @Test
    fun outgoing_message_internal_getTemplate() {
        invokePrivateMethod(testScanner, "internal_getTemplate")

        assertOutgoingMessageIs(GET_TEMPLATE_REQUEST)
        assertOutgoingMessageIs(GET_TEMPLATE_FRAGMENT_0, 1)
        assertOutgoingMessageIs(GET_TEMPLATE_FRAGMENT_1, 2)
        assertOutgoingMessageIs(GET_TEMPLATE_FRAGMENT_2, 3)
    }

    @Test
    fun outgoing_message_connection_getBank() {
        testScanner.connection_getBank()
        assertOutgoingMessageIs(GET_RUNNING_BANK)
    }

    @Test
    fun outgoing_message_connection_setBank() {
        testScanner.connection_setBank(Scanner.BANK_ID.PRODUCTION.id, 1.toChar(), 0.toChar())
        assertOutgoingMessageIs(SET_RUNNING_BANK)
    }

    @Test
    fun outgoing_message_connection_sendOtaMeta() {
        testScanner.connection_sendOtaMeta(mockMeta[0], mockMeta[1].toShort())
        assertOutgoingMessageIs(META_DATA_COMMAND)
    }

    @Test
    fun outgoing_message_connection_sendOtaPacket() {
        testScanner.connection_sendOtaPacket(0, OTA_PACKET_0.length, OTA_PACKET_0)
        assertOutgoingMessageIs(SET_OTA_PACKET_0)
    }

    @Test
    fun outgoing_message_connection_crashVero() {
        testScanner.connection_crashVero(0.toChar())
        assertOutgoingMessageIs(CRASH_VERO_0)
    }

    private fun callBlocking(
        function: (ScannerCallback) -> Unit,
        scannerCallback: ScannerCallback? = null,
    ): Boolean {
        val result = LinkedBlockingQueue<Boolean>()

        function(
            wrappedScannerCallback(onSuccess = {
                scannerCallback?.onSuccess()
                result.put(true)
            }, onFailure = {
                scannerCallback?.onFailure(it)
                result.put(false)
            }),
        )

        // TODO loopers in Scanner.wrapCallback mean we have to wakeUpLooper.
        Thread.sleep(100)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        return result.take()
    }

    private fun assertOnSuccessCallback(function: (ScannerCallback) -> Unit) {
        var x = 0
        callBlocking(
            { function(it) },
            wrappedScannerCallback(
                onSuccess = { x = 1 },
                onFailure = {},
            ),
        )
        Assert.assertEquals(1, x)
    }

    private fun assertOutgoingMessageIs(
        msgToScanner: String,
        position: Int = 0,
    ) {
        assert(receivedValues[position].contentEquals(byteArrayFromHexString(msgToScanner)))
    }

    private fun invokePrivateMethod(
        scannerObject: Scanner,
        name: String,
    ) {
        val method = Scanner::class.java.getDeclaredMethod(name)
        method.isAccessible = true
        method.invoke(scannerObject)
    }

    companion object {
        // Expected outgoing messages to the scanner
        private const val WAKE_UP_MESSAGE = "fafafafa0c001000f5f5f5f5"
        private const val SENSOR_INFO_REQUEST = "fafafafa0c000000f5f5f5f5"
        private const val RESET_UI_COMMAND = "fafafafa1600020001010000000000000000f5f5f5f5"
        private const val GET_IMAGE_QUALITY = "fafafafa0c000b00f5f5f5f5"
        private const val GET_TEMPLATE_REQUEST = "fafafafa0c000c00f5f5f5f5"
        private const val GET_TEMPLATE_FRAGMENT_0 = "fafafafa0e0016000000f5f5f5f5"
        private const val GET_TEMPLATE_FRAGMENT_1 = "fafafafa0e0016000100f5f5f5f5"
        private const val GET_TEMPLATE_FRAGMENT_2 = "fafafafa0e0016000200f5f5f5f5"
        private const val GET_RUNNING_BANK = "fafafafa0c001e00f5f5f5f5"
        private const val SET_RUNNING_BANK = "fafafafa0f001f00010100f5f5f5f5"
        private const val CRASH_VERO_0 = "fafafafa0d00200000f5f5f5f5"

        private const val META_FILE_SIZE = 108848
        private const val META_CRC = 64851
        private val mockMeta = arrayListOf(META_FILE_SIZE, META_CRC)
        private const val META_DATA_COMMAND = "fafafafa12001d0030a9010053fdf5f5f5f5"

        private const val OTA_PACKET_0 = ":020000040000FA\n" +
            ":10000000B45100208D03001BB902001BE54A001B00\n" +
            ":10001000C102001BC502001BC902001BD254FF3DD8\n" +
            ":10002000000000000000000000000000191B001B81\n" +
            ":10003000D102001B00000000D11B001B0D1C001B87\n" +
            ":10004000DD02001B00000000E102001B00000000B8\n" +
            ":10005000E502001BE902001BED02001BF102001B80\n" +
            ":10006000093A001B353A001BFD02001B0103001B6F\n" +
            ":100070000503001B0903001B0D03001B1103001BDC\n" +
            ":100080001503001B1903001B1D03001B2103001B8C\n" +
            ":10009000000000002503001B2903001B2D03001B8B\n" +
            ":1000A000857A001B6529001BB17A001BDD7A001BD5\n" +
            ":1000B0004103001B4503001B00000000000000007E\n" +
            ":1000C0005F54001B6954001B7354001B7D54001BBC\n" +
            ":1000D0008754001B9154001B9B54001BA554001B0C\n" +
            ":1000E0006903001B6D03001B7103001B7503001BDC\n" +
            ":1000F00000000000000000007903001B7D03001BCE\n"
        private const val SET_OTA_PACKET_0 = "fafafafa34031c0000000000d00200003" +
            "a30323030303030343030303046410a3a31303030303030304234" +
            "35313030323038443033303031424239303230303142453534413" +
            "030314230300a3a31303030313030304331303230303142433530" +
            "32303031424339303230303142443235344646334444380a3a313" +
            "03030323030303030303030303030303030303030303030303030" +
            "30303030313931423030314238310a3a313030303330303044313" +
            "03230303142303030303030303044313142303031423044314330" +
            "30314238370a3a313030303430303044443032303031423030303" +
            "0303030304531303230303142303030303030303042380a3a3130" +
            "30303530303045353032303031424539303230303142454430323" +
            "0303142463130323030314238300a3a3130303036303030303933" +
            "41303031423335334130303142464430323030314230313033303" +
            "0314236460a3a3130303037303030303530333030314230393033" +
            "303031423044303330303142313130333030314244430a3a31303" +
            "03038303030313530333030314231393033303031423144303330" +
            "303142323130333030314238430a3a31303030393030303030303" +
            "03030303032353033303031423239303330303142324430333030" +
            "314238420a3a31303030413030303835374130303142363532393" +
            "03031424231374130303142444437413030314244350a3a313030" +
            "30423030303431303330303142343530333030314230303030303" +
            "03030303030303030303037450a3a313030304330303035463534" +
            "30303142363935343030314237333534303031423744353430303" +
            "14242430a3a313030304430303038373534303031423931353430" +
            "3031423942353430303142413535343030314230430a3a3130303" +
            "04530303036393033303031423644303330303142373130333030" +
            "3142373530333030314244430a3a3130303046303030303030303" +
            "03030303030303030303030373930333030314237443033303031" +
            "4243450a000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000" +
            "00000000000000000000000000000000000000000000000000000" +
            "000000000f5f5f5f5"
    }
}
