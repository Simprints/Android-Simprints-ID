package com.simprints.id.data.prefs.settings.fingerprint.serializers

import com.simprints.id.data.db.subject.domain.FingerIdentifier
import com.simprints.id.tools.serializers.Serializer

class FingerprintsToCollectSerializer : Serializer<List<FingerIdentifier>> {

    override fun serialize(value: List<FingerIdentifier>): String =
        value.joinToString(delimiter) { it.toString() }

    override fun deserialize(string: String): List<FingerIdentifier> =
        string.replace(" ", "").replace("\n", "").replace("\r", "")
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                try {
                    FingerIdentifier.valueOf(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

    companion object {
        const val delimiter = ","
    }
}
