package com.simprints.id.data.prefs.settings.fingerprint.serializers

import com.simprints.id.data.prefs.settings.fingerprint.models.ScannerGeneration
import com.simprints.id.tools.serializers.Serializer

class ScannerGenerationsSerializer : Serializer<List<ScannerGeneration>> {

    override fun serialize(value: List<ScannerGeneration>): String = StringBuilder().apply {
        value.forEachIndexed { index, scannerGeneration ->
            append(scannerGeneration.toString())
            if (index != value.size - 1) {
                append(delimiter)
            }
        }
    }.toString()

    override fun deserialize(string: String): List<ScannerGeneration> =
        string.replace(" ", "").replace("\n", "").replace("\r", "")
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                try {
                    ScannerGeneration.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

    companion object {
        const val delimiter = ","
    }
}
