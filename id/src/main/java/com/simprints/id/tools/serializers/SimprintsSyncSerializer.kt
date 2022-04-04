package com.simprints.id.tools.serializers

import com.simprints.id.domain.SimprintsSyncSetting

class SimprintsSyncSerializer : Serializer<List<SimprintsSyncSetting>> {

    override fun serialize(value: List<SimprintsSyncSetting>): String =
        value.joinToString(separator = separator)

    override fun deserialize(string: String): List<SimprintsSyncSetting> =
        string.replace(Regex("[ \n\r\t]"), "")
            .split(separator)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                when (it) {
                    ALL -> SimprintsSyncSetting.ALL
                    ONLY_BIOMETRICS -> SimprintsSyncSetting.ONLY_BIOMETRICS
                    ONLY_ANALYTICS -> SimprintsSyncSetting.ONLY_ANALYTICS
                    NONE -> SimprintsSyncSetting.NONE
                    else -> null
                }
            }

    companion object {
        private const val separator = ","
        private const val ALL = "ALL"
        private const val ONLY_BIOMETRICS = "ONLY_BIOMETRICS"
        private const val ONLY_ANALYTICS = "ONLY_ANALYTICS"
        private const val NONE = "NONE"
    }
}
