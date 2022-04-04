package com.simprints.id.tools.serializers

import com.simprints.id.domain.CosyncSetting

class CosyncSerializer : Serializer<List<CosyncSetting>> {

    override fun serialize(value: List<CosyncSetting>): String =
        value.joinToString(separator = separator)

    override fun deserialize(string: String): List<CosyncSetting> =
        string.replace(Regex("[ \n\r\t]"), "")
            .split(separator)
            .filter { it.isNotEmpty() }
            .mapNotNull {
                when (it) {
                    ALL -> CosyncSetting.ALL
                    ONLY_BIOMETRICS -> CosyncSetting.ONLY_BIOMETRICS
                    ONLY_ANALYTICS -> CosyncSetting.ONLY_ANALYTICS
                    NONE -> CosyncSetting.NONE
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
