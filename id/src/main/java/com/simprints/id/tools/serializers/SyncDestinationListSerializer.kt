package com.simprints.id.tools.serializers

import com.simprints.id.domain.SyncDestinationSetting

class SyncDestinationListSerializer : Serializer<List<SyncDestinationSetting>> {

    override fun serialize(value: List<SyncDestinationSetting>): String = value.joinToString(separator = ",")

    override fun deserialize(string: String): List<SyncDestinationSetting> =
        string.replace(Regex("[ \n\r\t]"), "")
            .split(delimiter)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                when (it) {
                    COMMCARE -> SyncDestinationSetting.COMMCARE
                    SIMPRINTS -> SyncDestinationSetting.SIMPRINTS
                    else -> null
                }
            }

    companion object {
        const val delimiter = ","
        private const val COMMCARE = "COMMCARE"
        private const val SIMPRINTS = "SIMPRINTS"
    }
}
