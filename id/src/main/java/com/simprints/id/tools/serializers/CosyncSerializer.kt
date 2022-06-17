package com.simprints.id.tools.serializers

import com.simprints.id.data.prefs.settings.SettingsPreferencesManagerImpl
import com.simprints.id.domain.CosyncSetting

class CosyncSerializer : Serializer<CosyncSetting> {

    override fun serialize(value: CosyncSetting): String = value.name

    override fun deserialize(string: String): CosyncSetting {
        return when (string.replace(Regex("[ \n\r\t]"), "")) {
            ALL -> CosyncSetting.COSYNC_ALL
            ONLY_BIOMETRICS -> CosyncSetting.COSYNC_ONLY_BIOMETRICS
            ONLY_ANALYTICS -> CosyncSetting.COSYNC_ONLY_ANALYTICS
            NONE -> CosyncSetting.COSYNC_NONE
            else -> SettingsPreferencesManagerImpl.COSYNC_SYNC_SETTINGS_DEFAULT
        }
    }

    companion object {
        private const val ALL = "ALL"
        private const val ONLY_BIOMETRICS = "ONLY_BIOMETRICS"
        private const val ONLY_ANALYTICS = "ONLY_ANALYTICS"
        private const val NONE = "NONE"
    }
}
