package com.simprints.id.tools.serializers

import com.simprints.id.domain.SyncDestinationSetting

class SyncDestinationListSerializer : Serializer<List<SyncDestinationSetting>> {

    override fun serialize(value: List<SyncDestinationSetting>): String = value.joinToString(separator = separator)

    override fun deserialize(string: String): List<SyncDestinationSetting> =
        string.replace(Regex("[ \n\r\t]"), "")
            .split(separator)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                when (it) {
                    COMMCARE -> SyncDestinationSetting.COMMCARE
                    SIMPRINTS -> SyncDestinationSetting.SIMPRINTS
                    else -> null
                }
            }

    companion object {
        private const val separator = ","
        private const val COMMCARE = "COMMCARE"
        private const val SIMPRINTS = "SIMPRINTS"
    }
}
