package com.simprints.fingerprint.infra.scanner

import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcAdapter
import com.simprints.fingerprint.infra.scanner.nfc.ComponentNfcTag
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

class NfcManager @Inject constructor(
    private val nfcAdapter: ComponentNfcAdapter,
) {
    val channelTags: Channel<ComponentNfcTag> = Channel()

    fun doesDeviceHaveNfcCapability(): Boolean = !nfcAdapter.isNull()

    fun isNfcEnabled(): Boolean = !nfcAdapter.isNull() && nfcAdapter.isEnabled()

    suspend fun enableReaderMode(
        activity: Activity,
        flags: Int = ComponentNfcAdapter.FLAG_READER_NFC_A or ComponentNfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
        extras: Bundle? = null,
    ) {
        if (nfcAdapter.isNull()) return
        nfcAdapter.enableReaderMode(activity, { tag: ComponentNfcTag? ->
            (activity as LifecycleOwner).lifecycleScope.launch {
                tag?.let { channelTags.send(it) }
            }
        }, flags, extras)
    }

    fun disableReaderMode(activity: Activity) {
        if (nfcAdapter.isNull()) return
        nfcAdapter.disableReaderMode(activity)
    }

    /**
     * Expect an NFC tag with type application/vnd.bluetooth.ep.oob (AKA Bluetooth Easy Pair).
     * The MAC address is found 15 * 4 = 60 bytes into the payload of the loaded data.
     * Note that the MAC address will appear reversed on the chip, so we reverse it here.
     *
     * @return The MAC address as a hex string with semicolons
     *
     * @throws IOException if and issue occurs during communication with the tag
     * @throws IllegalArgumentException if the supplied tag is not a Bluetooth Easy Pair tag with
     * a valid MAC address
     */
    fun readMacAddressDataFromBluetoothEasyPairTag(tag: ComponentNfcTag?): String {
        val mifare = nfcAdapter.getMifareUltralight(tag)
            ?: throw IllegalArgumentException("Could not get mifare connection from tag")

        val payload = mifare.use {
            it.connect()
            it.readPages(BT_EASY_PAIR_MAC_ADDRESS_PAGE_OFFSET_POSITION)
        }

        return interpretDataAsMacAddress(payload)
    }

    private fun interpretDataAsMacAddress(payload: ByteArray): String = try {
        payload
            .sliceArray(0..5)
            .reversedArray()
            .toHexStringWithColons()
            .dropLast(1)
    } catch (e: IndexOutOfBoundsException) {
        throw IllegalArgumentException("Invalid data on tag")
    }

    private fun ByteArray.toHexStringWithColons(): String = StringBuilder()
        .apply {
            this@toHexStringWithColons.forEach { append(String.format("%02X:", it)) }
        }.toString()

    companion object {
        private const val BT_EASY_PAIR_MAC_ADDRESS_PAGE_OFFSET_POSITION = 15
    }
}
