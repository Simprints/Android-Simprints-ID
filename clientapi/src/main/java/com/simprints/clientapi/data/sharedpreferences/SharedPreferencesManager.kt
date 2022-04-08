package com.simprints.clientapi.data.sharedpreferences

import com.simprints.core.domain.modality.Modality
import com.simprints.id.domain.CosyncSetting
import com.simprints.id.domain.SyncDestinationSetting


interface SharedPreferencesManager {
    val syncDestinationSettings: List<SyncDestinationSetting>

    val cosyncSyncSettings: List<CosyncSetting>

    val modalities: List<Modality>

    fun stashSessionId(sessionId: String)

    fun peekSessionId(): String

    fun popSessionId(): String

}
