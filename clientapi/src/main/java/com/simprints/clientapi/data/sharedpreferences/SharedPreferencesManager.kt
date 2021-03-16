package com.simprints.clientapi.data.sharedpreferences

import com.simprints.id.domain.SyncDestinationSetting
import com.simprints.id.domain.modality.Modality


interface SharedPreferencesManager {
    val syncDestinationSettings: List<SyncDestinationSetting>

    val modalities: List<Modality>

    fun stashSessionId(sessionId: String)

    fun peekSessionId(): String

    fun popSessionId(): String

}
